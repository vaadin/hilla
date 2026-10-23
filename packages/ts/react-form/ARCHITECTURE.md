# React form binding

## Overview

`@vaadin/hilla-react-form` is the React face of the form binder in
`@vaadin/hilla-lit-form`. All of it is in [`src/index.ts`](src/index.ts): three
hooks, `useForm`, `useFormPart` and `useFormArrayPart`, and the `field`
directive they return. The hooks turn binder change events into re-renders,
and `field` attaches a field strategy to an element through a React ref. The
binder itself (nodes, values, validation, submit, field strategies) is
described in [`../lit-form/ARCHITECTURE.md`](../lit-form/ARCHITECTURE.md).

## Design goals

- **No binder logic of its own.** Values, validation, dirty and visited state,
  and how a value gets in and out of an element all belong to lit-form. The
  hooks only call into it.
- **React state is derived, not copied.** The only React state is a render
  counter. Every render reads the result fresh from the `BinderNode`, so React
  and the binder can't disagree.

## `useForm`

- Creates one `BinderRoot` per component instance, in a `useMemo` whose
  dependency never changes (it is the imported `Model`, not `modelClass`). A
  different `modelClass` on a later render is ignored.
- `onSubmit` and `onChange` are copied on every render into a config object
  the binder keeps a reference to, so the binder always calls the latest
  callbacks.
- On mount it subscribes to the root's `CHANGED` event, which bumps the render
  counter, and calls `clear()` once to initialize validation.
- The result is a snapshot of the root node. `value` and `defaultValue` are
  read live; `dirty`, `errors`, `visited` and the rest are as of the render.

## Re-renders come from the root

A `BinderNode` is an `EventTarget`, and `CHANGED` does not bubble. Changes that
go through `_update` (value writes, `validate()`, `read`, submit) end in a
`CHANGED` on the root, so the `useForm` component re-renders, and with it the
children that call the other hooks.

`useFormPart` and `useFormArrayPart` do not subscribe to their own node. A
`CHANGED` dispatched only on a nested node (for example `setVisited`, or
`setValidators`) re-renders nothing until something else updates the root.

## The `isRendering` guard

Reading a node during render can write to the binder: `getBinderNode` may
create the node and initialize its value, and `field` pushes the node state
into the element. That would dispatch `CHANGED` and set state on the `useForm`
component while another one renders. A module-level `isRendering` flag,
set around those reads, makes every update callback a no-op meanwhile.

## The `field` directive

`useFields` keeps a `WeakMap` from model to field state, per node the hook was
called for.

- The `ref` callback picks the strategy with `getDefaultFieldStrategy` and
  wires input, change and blur handlers. It does not call
  `BinderRoot.getFieldStrategy`, so overriding that has no effect in React.
- Input writes the converted element value to the node; change also
  validates; blur also validates and marks the node visited.
- Every render (every `field(model)` call) pushes value, `required`, the first
  own error and `invalid` from the node into the strategy, only where they
  differ.

## `useFormArrayPart`

It gets its `items` by iterating the model, which only the legacy lit-form
`ArrayModel` supports, and its type parameter accepts only that model. A
generated array model from `@vaadin/hilla-models` is not iterable: forced
through, it gets no items. With generated models, iterate with `m.items`
instead.
