# Form models

## Overview

A model describes the shape of a value and its constraints, separately from any
value. The generator writes one model per Java bean or enum an endpoint uses,
under `frontend/generated/`:

```ts
const PersonModel = m
  .extend(AbstractEntityModel)
  .object<Person>('Person')
  .property('id', m.meta(m.optional(NumberModel), { jvmType: 'java.lang.Long' }))
  .property('firstName', m.meta(m.constrained(StringModel, NotBlank()), { jvmType: 'java.lang.String' }))
  .property('email', m.meta(m.constrained(m.optional(StringModel), Email()), { jvmType: 'java.lang.String' }))
  .property(
    'tags',
    m.meta(m.constrained(m.array(StringModel), Size({ max: 5 })), {
      annotations: [{ jvmType: 'jakarta.persistence.ElementCollection' }],
      jvmType: 'java.util.List',
    }),
  )
  .property('addresses', m.meta(m.array(AddressModel), { jvmType: 'java.util.List' }))
  .property('phones', m.meta(m.record(StringModel), { jvmType: 'java.util.Map' }))
  .property('manager', m.optional(m.self))
  .build();
```

A model is a plain object, built once at module load. Its properties are models
too, but each one is only built when it is first read, so a model of a large or
recursive type costs nothing until something walks it. A consumer — the binder,
a signal, a grid — attaches the model to its own value holder and gets a copy
that reads from it.

The examples below use this `PersonModel`, with `AbstractEntityModel` declaring
`version` and `id`, and `AddressModel` declaring `id`, `street` and an enum
`kind`. Each one was run against the package.

## Design goals

- **Models are reflection data about the Java types.** They carry what a client
  needs to know about a value at run time that its TypeScript type erases: the
  properties, their models, constraints, optionality, the Java type and the
  annotations.
- **The binder is the main consumer.** It builds the empty form value from
  `$defaultValue`, which makes the default value the most important thing a
  model provides, turns the constraints into validators, and walks the property
  and item models to create its nodes. See the
  [binder note](../lit-form/ARCHITECTURE.md).
- **Other tools read them too**, such as `ModelInfo` in react-crud, which builds
  grid columns and form fields from the property names, `jvmType` and
  annotations.
- **Navigating to a nested value is plain, checked code.** The APIs that take
  a nested key — the binder's `for` and `field`, and the AutoForm layouts in
  react-crud built on them — take a model instead of a string path:
  `binder.for(model.address.street)` follows the same path as the data, as
  ordinary TypeScript the compiler checks and the IDE completes at every step.
  A renamed or removed Java property breaks the build instead of a string at
  run time.
- **So reading the reflection has to be easy.** Everything is reachable with
  plain JavaScript: destructuring, `for...in`, `instanceof` and the prototype
  chain. The `$`-symbol keys keep the reflection out of the way of the property
  names, which are those of the Java bean.

## Reading a model

The reflection is kept under symbols exported by the package:

- `$name`: the name of the model, such as `'Person'` or `'Array<Address>'`.
- `$key` and `$owner`: the key of a property model and the model it belongs to.
- `$defaultValue`, `$optional`, `$constraints`, `$meta`.
- `$itemModel` of an array model, `$valueModel` of a record model, `$enum` of an
  enum model, `$members` of a union and `$literal` of a literal model.

Property models are reached like the properties of a value, and are cached:

```ts
const { firstName, addresses } = PersonModel;
firstName[$key]; // 'firstName'
firstName[$owner] === PersonModel; // true
PersonModel.firstName === firstName; // true
addresses[$itemModel] === AddressModel; // true
PersonModel.manager.manager.firstName; // m.self recurses as deep as needed
```

A property model belongs to the model it was read from: `EmployeeModel.firstName`
is a different object than `PersonModel.firstName`, owned by `EmployeeModel`.

Property names are the enumerable string keys. `for...in` lists the inherited
ones too, own properties first, and a redeclared property once; `Object.keys`
lists only those the model itself declares:

```ts
for (const key in PersonModel) {
} // id, firstName, email, tags, addresses, phones, manager, version
Object.keys(PersonModel); // id, firstName, email, tags, addresses, phones, manager
```

Walking the chain one level at a time with `Object.keys` gives each level's own
properties, and meets a redeclared one (`id` here) on both levels.

The prototype chain is the type hierarchy. A model built with `m.extend` has its
base as its prototype, and every wrapper (`m.optional`, `m.constrained`,
`m.meta`) adds one more level on top of what it wraps:

```ts
Object.getPrototypeOf(PersonModel) === AbstractEntityModel; // true
Object.getPrototypeOf(AbstractEntityModel) === ObjectModel; // true
Object.getPrototypeOf(ObjectModel) === Model; // true, and Model's is null

PersonModel instanceof AbstractEntityModel; // true
PersonModel.email instanceof StringModel; // true, through the wrappers
PersonModel.tags instanceof ArrayModel; // true
PersonModel.addresses[$itemModel].kind instanceof EnumModel; // true
```

Use `instanceof` to ask about a base, not `Object.getPrototypeOf`: a base given
as a function (`m.extend(() => PersonModel)`, see [Cycles](#cycles)) is not in the
chain.

Constraints are data: a name and attributes.

```ts
const [notBlank] = PersonModel.firstName[$constraints];
notBlank.name; // 'NotBlank'
m.isConstraint(notBlank, NotBlank); // true
PersonModel.tags[$constraints][0].attributes; // { min: 0, max: 5 }, defaults filled in
```

Metadata and optionality:

```ts
PersonModel.email[$meta]; // { jvmType: 'java.lang.String' }
PersonModel.tags[$meta]?.annotations; // [{ jvmType: 'jakarta.persistence.ElementCollection' }]
PersonModel[$meta]; // undefined: a bean's model has no jvmType
PersonModel.email[$optional]; // true
PersonModel.firstName[$optional]; // false
```

The default value is built on every read, so each consumer gets its own object:

```ts
PersonModel[$defaultValue];
// { id: undefined, firstName: '', email: undefined, tags: [], addresses: [],
//   phones: {}, manager: undefined, version: NaN }
PersonModel[$defaultValue] !== PersonModel[$defaultValue]; // true
AddressModel[$defaultValue].kind; // 'PRIVATE', the first constant of the enum
PersonModel.email[$defaultValue]; // '': an optional model keeps the default of what it wraps
```

An array model has one item model, `$itemModel`, which describes every item.
Models for the actual items need a value, so they come from an attached model
through `m.items`; `m.getValue` reads the value at a model:

```ts
const person = { ...PersonModel[$defaultValue], addresses: [{ street: 'Main St' }, { street: 'Side St' }] };
const model = m.attach(PersonModel, () => ({ value: person }));

const [first, second] = m.items(model.addresses);
first[$key]; // 0
first[$owner] === model.addresses; // true
m.getValue(first.street); // 'Main St'
[...m.items(model.addresses)][0] === first; // true: item models are cached

m.getValue(PersonModel.firstName); // '': a detached model reads its default value
```

### Attached and detached models

Any model can be attached, at the top of the data hierarchy, to whatever holds
the state: the binder attaches the form's model to itself, and another state
manager, such as a full-stack signal, could do the same. `m.getValue` of the
attached model and of every model below it then reads the actual value from
the target's current state, on every call:

```ts
const target = { value: person };
const model = m.attach(PersonModel, () => target);
m.getValue(model.firstName); // person.firstName
target.value = otherPerson;
m.getValue(model.firstName); // otherPerson.firstName
```

A model that is not attached is detached: `m.getValue` returns its built-in
default, a value that satisfies its type — `''` for a string, `NaN` for a
number, `[]` for an array, and an object with all of its required keys for an
object model.

## Prototypes and symbols

`Model` is the root, an object with a `null` prototype that defines every
`$`-symbol with its fallback: `$owner` is a detached target, `$optional` is
`false` and `$constraints` is empty. Everything else is created with
`Object.create` on top of another model, and defines only what differs. The
built-in models (`PrimitiveModel`, `StringModel`, `NumberModel`,
`BooleanModel`, `ArrayModel`, `ObjectModel`, `RecordModel`, `EnumModel`) live
in [`src/models.ts`](src/models.ts); their default values are `''`, `NaN`,
`false`, `[]` and `{}`. A number defaults to `NaN` rather than `0` so that an
empty form neither breaks the `number` type nor starts from an arbitrary
number; see the [binder note](../lit-form/ARCHITECTURE.md#empty-numbers) for
how the binder handles it.

- Property names and symbol keys never collide, so a bean property may have any
  name.
- `Symbol.hasInstance` on `Model` makes `instanceof` mean "is this model or
  derives from it", so models work as types without classes. `m.union`
  overrides it: a value is an instance when it is one of any member.
- A model attaches to a `Target` — anything with a `value`, such as a binder or
  a signal — with `m.attach`. The attached model is a new level on top, named
  `@` plus the name, whose `$owner` getter returns the target. Its property
  models own up to it, so `m.getValue` reads a property's value by following
  `$owner` up to the target and the `$key`s back down. A model is never
  attached to another model.

## Builders

The `m` namespace ([`src/m.ts`](src/m.ts) and
[`src/converters.ts`](src/converters.ts)) is the public way to make models; the
builders in [`src/modelBuilders.ts`](src/modelBuilders.ts) are internal.

- `ObjectModelBuilder.property` defines an enumerable getter. The first read
  builds the property model on top of the given one, with `$key` and `$owner`
  set, and keeps it in the `propertyRegistry` `WeakMap` under the model it was
  read from. That is why the property models of a subtype, or of an attached
  model, are their own.
- The default value of an object model is computed by its getter: `for...in`
  over the model, `undefined` for an optional property, the property model's
  `$defaultValue` otherwise. Inherited properties take part, and the default
  values of the properties are read only then.
- `m.items` keeps the item models of an array model in a `WeakMap` and reuses
  them by index, so a list re-renders against the same models; the list is cut
  to the length of the value.
- The converters (`m.optional`, `m.array`, `m.record`, `m.constrained`,
  `m.meta`) each return a new level and never change their argument. They also
  accept another converter instead of a model, which is what lets
  `m.optional(m.self)` refer to the model being built.

### Cycles

Models that refer to each other form import cycles between their modules, and
a module evaluated second would read an uninitialized binding. Three tools defer
the read:

- `m.self` refers to the model being built itself.
- `m.lazy(() => OtherModel)` defers a property's model until the property is
  first read.
- `m.extend(() => BaseModel)` defers the base. The model is then built on a
  `Proxy` stand-in (`lazyBase`) that serves the base's properties, inherited
  ones included, as its own and reports what is below them as its prototype, so
  a walk does not meet them twice. The provider is kept under `$lazyParent`,
  which `Symbol.hasInstance` follows; that is why `instanceof` works and
  `Object.getPrototypeOf` does not show the base.

Every cycle has to pass through a property whose default value does not read
the other model: an optional one (`undefined`), an array (`[]`) or a record
(`{}`). Otherwise building the default value overflows the stack.

## Constraints

A constraint is data, not a validator: a function made by `ConstraintBuilder`
that, called with attributes, returns an object with `name` and `attributes`
whose prototype is the function itself, which `m.isConstraint` checks.

- Attributes that are not given fall back to the declared defaults; one without
  a default is left out rather than set to `undefined`, so consumers can spread
  them over defaults of their own.
- A constraint declares the model it applies to. `m.constrained` calls
  `$assertSupportedModel`, which throws when the model is not an instance of it
  — `NotBlank` on a `NumberModel`, say. A generated module with such a
  constraint fails when it is evaluated.
- The binder matches each constraint against the built-in ones with
  `m.isConstraint` and creates the validator class of the same name
  (`getConstraintValidator` in lit-form). Any other constraint throws
  `Unsupported constraint`: a custom one, and `PastOrPresent` and
  `FutureOrPresent`, which have no validator.

## Metadata and optionality

`$meta` holds `jvmType`, the Java type of the value, and `annotations`, each
with its `jvmType` and optional `attributes`. A class given as an attribute
value comes as `{ jvmType }`. `$meta` is inherited like everything else, so a
model without metadata of its own shows its base's.

`$optional` says the value may be absent: `undefined` in TypeScript, `null` in
Java. An optional property has `undefined` in the default value of its owner,
but its own `$defaultValue` is still that of the model it wraps.

## What the generator writes

`ModelWriter` and `FormModelWriter` in
[`packages/java/typescript-generator`](../../java/typescript-generator/src/main/java/com/vaadin/hilla/generator/typescript)
write the models.

- A property's model is wrapped from the inside out: `m.array` or `m.record`
  around the value model, then `m.optional`, then `m.constrained`, then
  `m.meta`. So `$meta` is the outermost level and `$optional` is below the
  constraints.
- A bean's own model is `m.object`, or `m.extend(Base).object` for the first
  supertype. A property of the bean's own class is `m.self`; a reference that
  closes an import cycle is `m.lazy`, and a base that does is
  `m.extend(() => Base)`. A polymorphic subtype gets its discriminator property
  as `m.literal`, or a `m.union` of literals, so its default value names the
  subtype.
- Only constraints listed in `ModelConstraints` are written, and only on the
  kinds of model the package accepts them on, with the attributes it declares.
  `PastOrPresent` and `FutureOrPresent` are left out, since the binder cannot
  validate them. What the server validates is unaffected.
- `jvmType` is written only for primitives and `java.*` types (and arrays of
  them), but not `java.lang.Object`; a bean's model has none, so the names of
  application classes do not reach the client.
