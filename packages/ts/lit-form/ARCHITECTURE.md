# Binder lifecycle

## Overview

A binder holds the value of one form and everything a form UI needs to know
about it: whether it was edited, which fields were visited, and which values
are invalid. It is built from a model, which describes the shape of the value
and its constraints; models are described in
[../models/ARCHITECTURE.md](../models/ARCHITECTURE.md).

The life of a form goes like this: the binder is created from a model and
starts with the model's empty value; `read` loads a value; fields write the
user's edits into it; validators run as fields change and before submit;
`submitTo` sends the value to an endpoint and maps the server's validation
errors back onto the fields. The sections below follow that order.

The React hooks in `@vaadin/hilla-react-form` are a thin layer over the same
binder: see [../react-form/ARCHITECTURE.md](../react-form/ARCHITECTURE.md).

## Design goals

- **Framework-neutral state.** All form state lives in `BinderRoot` and its
  `BinderNode`s, which are plain `EventTarget`s. Lit (`Binder` and the `field`
  directive) and React (`useForm`) only differ in how they turn a change into
  a re-render and how they attach to elements.
- **Immutable values.** The form value is never mutated. Every write builds a
  new value from the edited node up to the root, so a change is detected by
  identity, and a value handed to the binder or taken from it stays as it was.
- **Validation driven by the model.** The validators of a node come from its
  model's constraints, so the Bean Validation annotations on the Java type are
  checked in the browser without any form code.
- **Pristine state a form UI can trust.** `dirty` is `value !== defaultValue`
  on every node. Operations that don't change what the user entered, such as
  adding an empty array item, keep the form pristine.

## Models

The binder works on models from `@vaadin/hilla-models` (`Model`), which is
what the generator writes. They carry constraints as data (`$constraints`),
and their empty value is `$defaultValue`. Every signature takes a `Model`.

- [src/Models.ts](src/Models.ts) only holds types: `Value`, the value type of
  a model, and `ArrayItemModel`, the item model of an array model.
- This package defines no models of its own. A hand-written model is built
  with the `m` builders of `@vaadin/hilla-models`.

## Binder, BinderRoot and BinderNode

- `BinderNode` holds the state of one model instance: its errors, its
  validators, `visited` and the validity state of its field. It reads and
  writes its value through its parent.
- `BinderRoot` is the node of the top-level model. It holds the form value,
  the default value, the submit and validation state, and the configuration
  (`onChange`, `onSubmit`, `context`).
- `Binder` is the Lit flavour: a `BinderRoot` whose context is the view
  element, and whose `onChange` defaults to the view's `requestUpdate()`.

### Creation

The root's constructor gets the top-level model.

- The model is attached to the binder with `m.attach`, so its `$owner` is the
  binder. This makes a per-form copy of the generated model, which is shared
  between forms.
- The root then fills itself with the model's empty value, which it keeps for
  `clear()`.

### One node per model instance

`getBinderNode` keeps a `WeakMap` from model instance to node, and
`binder.for(model)` goes through it. Since the models cache their property
and item models, the same path always gives the same node, and node state
survives re-renders.

- A node's parent is the node of its model's `$owner`; the root is the node
  whose owner is not a model.
- Array item models are cached by index. Their nodes, and so their errors and
  `visited`, belong to a position in the array, not to an item: after an item
  is removed, the next item's node is the one the removed item had.

### Names

A node's `name` is the path of `$key`s from the root, joined with dots
(`orders.0.description`). The `field` directive sets it as the field's `name`
attribute, and validation errors are matched to nodes by it. The root's name
is the empty string.

## Values

Only the root stores anything: `value` and `defaultValue`. A node's value and
default value are read from its parent's by `$key`.

### Writing a value

Setting `node.value` copies up: the parent object or array is copied with the
new entry (`updateObjectOrArrayKey`), then its parent, up to the root, which
gets a new value. The same walk updates the default value when it has to:

- If the node's new value equals its default value and the parent is
  pristine, the parent's value and default value are set to the same new
  object (`keepPristine`), so `dirty` stays false all the way up.
- If a new default value is given, both are copied up separately.
- Otherwise only the value is copied up, and the form becomes dirty.

### dirty and visited

- `dirty` compares by identity. A value that equals the default by content
  but was built anew is dirty.
- `visited` is set by the `field` directive on blur, and cleared for the
  whole tree by `read`.

### Lazy initialization

A node's value is created when the node is first used, not when the form is.
`initializeValue` first makes sure the parent has a value, then fills in the
node's empty value if it has none: always for the root or when forced (the
value setter forces it).

Models may be cyclic (a `Person` with a `manager: Person`), so the tree of
nodes is never walked by model alone. When looking for child nodes (for
validation and error distribution), an object node only yields the
properties whose key is in its default value. That is every required
property, plus the optional ones that a value was read into or that a field
was bound to.

### Empty numbers

The empty value of a number is `NaN` (`NumberModel`'s `$defaultValue`). It
satisfies the `number` type of the generated TypeScript, so an empty form
needs neither `undefined` in a non-optional property nor an arbitrary number
like `0` that the user would have to notice and clear. The binder keeps `NaN`
from showing or slipping through:

- The `field` directive renders `NaN` as an empty field.
- `IsNumber`, which every number node gets, reports `NaN`, so an untouched
  required number fails validation instead of being submitted.
- An optional number is `undefined` when empty, which `IsNumber` accepts.

## Read, reset, clear and submit

- `read(value)` sets it as the default value and the value, and clears the
  errors and `visited` of the whole tree. `read(undefined)` and `read(null)`
  clear the form.
- `reset()` reads the default value again, dropping the edits.
- `clear()` reads the empty value the binder was created with.
- `submit()` calls `submitTo` with the configured `onSubmit`, and does
  nothing without one.

### submitTo

1. Validate the whole form. If there are errors, throw a `ValidationError`
   without calling the endpoint.
2. Set `submitting` and notify, then call the endpoint method with the value,
   with the binder's context as `this`.
3. If it throws an `EndpointValidationError` with data, turn each entry into
   an error for a field and throw a `ValidationError`. Any other error is
   rethrown.
   - The entry's message is matched against the format `EndpointInvoker`
     writes for bean violations (`Object of type '…' has invalid property '…'
with value '…', validation error: '…'`), which gives the property path,
     the value as a string and the message. Otherwise the property is the
     entry's `parameterName` and the message is the whole message.
   - Bracket indexes become dots (`orders[0]` → `orders.0`), so the path
     matches the node names. An error whose path matches no node is only in
     the thrown `ValidationError`.
4. In `finally`, clear `submitting` and make the submitted value the default
   value. This happens on failure too: after a failed submit the form is no
   longer dirty, even though the server did not save anything.

## Arrays

- `appendItem` and `prependItem` add the item (or the item model's empty
  value) to the value and to the default value, so adding an empty row does
  not make the form dirty.
- `removeSelf` on an item node removes the item by index from both the
  value and the default value of the array.
- An item that is in the value but not in the default value (the array was
  set directly) gets an empty default value, cached per array node in
  `defaultArrayItemCache` so that it is the same object on every read and
  `dirty` stays stable.

## Validation

### Where validators come from

A node's validators are built once, when the node is created
(`getModelValidators`):

- Each of the model's `$constraints` is turned into the validator
  class in [src/Validators.ts](src/Validators.ts) of the same name
  (`getConstraintValidator`), with the constraint's attributes. A
  `NumberModel` gets an `IsNumber` validator first, which reports `NaN`, the
  value of a number field whose text doesn't parse.
- `PastOrPresent` and `FutureOrPresent` have no validator. The generator
  never writes them (see the models note), and any other unknown constraint
  throws.
- Applications add their own with `addValidator`, or replace the list with
  `validators`.

### Running validation

`node.validate()` runs the validators of the node, of all its descendants
and of all its ancestors, so that a validator on an object that checks two
of its fields together runs when one of them changes.

Every validator run goes through `BinderRoot.requestValidation`:

- All requests made in the same microtask share one start
  (`performValidation`), and `validating` stays true until the last of them
  has finished.
- A request for a model and validator that is already running gets the
  running promise.

`runValidator` then applies these rules:

- A node that is not required and has an empty value is not validated, as in
  Bean Validation. `IsNumber` and the validity state validator run anyway.
- `false` gives one error on the node; `true` or an empty array gives none.
- A `ValidationResult`, or an array of them, gives an error for each, with
  its `property` taken as relative to the node.
- A validator that throws gives one error, and the exception is logged.
- The message goes through `BinderRoot.interpolateMessageCallback`, when it
  is set, which is how an application translates validation messages.

When the bound field reports its own invalid state (`ValidityState`, read by
the `field` directive), the node also runs a `ValidityStateValidator`. On bad
input, such as text in a number field, only that one runs, since the value
the binder holds is not what the user sees.

### Distributing errors

`validate()` gives the errors to the node it was called on: the node keeps
the errors whose property is its own name, and passes those whose name
starts with its name down to its children, which do the same. `errors` is a
node's own errors plus those of its descendants, and `invalid` is whether it
has any. The errors of ancestor validators are only kept where their path
points into the validated subtree.

### Revalidation

Setting a node's value revalidates it only if it is invalid. A valid field is
not validated on every keystroke; that happens on change and on blur. An
invalid one is revalidated as it is edited, so its error goes away as soon as
it is fixed.

### required

A node is `required` when one of its validators has `impliesRequired`:
`Required` and its subclasses `NotNull`, `NotEmpty` and `NotBlank`, and
`Size` with a positive `min`. The `field` directive shows it on the field,
and it decides whether an empty value is validated at all.

## Change notification

- Any change of the value goes up the tree through the protected `_update`
  to the root, which calls `onChange` (with the old value) and dispatches
  `CHANGED` on itself.
- Changes of node state (errors, `visited`, validators) dispatch `CHANGED` on
  that node only.
- `defaultValue`, `submitting` and `validating` changes dispatch `CHANGED` on
  the root.
- `CHANGED` is one shared `Event` instance; listen with `CHANGED.type`.

In Lit, `onChange` re-renders the view, and the `field` directive reads the
binder state again. In React, `useForm` listens for `CHANGED` on the root.

## Fields

### Field strategies

A field strategy adapts one kind of element to the binder: how to read and
write its value, and how to show `required`, `invalid` and the error message.
`BinderRoot.getFieldStrategy` picks one; override it to support another
component. The default (`getDefaultFieldStrategy`) chooses by tag name:

- Vaadin components with a special value (`vaadin-checkbox`,
  `vaadin-combo-box`, `vaadin-date-time-picker` and so on) get their own
  strategy.
- `<input type="checkbox">` and `"radio"` get `CheckedFieldStrategy`.
- Any other element whose class has a `version` is taken as a Vaadin
  component (`VaadinFieldStrategy`); the rest get `GenericFieldStrategy`,
  which sets `required` and `invalid` as attributes.

`VaadinFieldStrategy` also listens to the components' `validated` and
`unparsable-change` events, because some edits (choosing a date in the
overlay, for example) don't fire `input`.

### The field directive

`field(model)` binds an element to the model's node.

- From the field: on `input`, it checks the element's validity, converts the
  value and writes it to the node, unless the element reports bad input. On
  `change` and `blur` it also validates the node, and on `blur` marks it
  visited.
- To the field: on every render, it pushes the node's name, value,
  `required`, first error message and `invalid` to the strategy, each only
  when it changed since the last render.

### String converters

Most fields give a string. Before writing it to the node, the directive
converts it by the model, with the converters in
[src/stringConverters.ts](src/stringConverters.ts) (boolean, number, enum). An empty number field gives
`undefined`, and an unparsable one `NaN`, which `IsNumber` reports.
