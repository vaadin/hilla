I would like to suggest the new design for Hilla models that replaces TypeScript class based definitions with objects defined using a builder API.

## Motivation

Hilla models were originally introduced to support form binding use cases. But recently we started to add more high-level data-oriented frontend developer productivity features to Hilla, such as `AutoGrid` and the upcoming `AutoForm`/`AutoCrud`, where we also take the data structure and metadata from the models. Some limitations of the current class-based models design became apparent:

- Accessing object properties is hard. It is expected that the described properties are easily available: for example, `NamedModel.name` references the `name` property of a `NamedModel`, and `Object.keys(SomeModel)` could iterate the keys. The model classes do not meet this expectation, and require an extra step for these use cases: either instantiation or a prototype access of some sort.
- The aforementioned model instantiation is hard. The model class constructor historically requires a value container in a form of either a form binder or the “parent” container model.
- Creating models manually is hard. Ideally an object model should use getters with a lazy initialization, and the generated models do use getters implemented with a helper.

## Usage

### Type description

Models are primarily used as runtime values that describe the underlying type. To illustrate, let us consider the following generated entity structure:

```typescript
interface Named {
  name: string;
}
```

The `name` property of this type could be referenced by indexing:

```typescript
type NamedNameType = Named["name"];
```

However, TypeScript types do not work as values. You cannot, for example, pass them to some React component props:

```tsx
function MyView() {
  return <AutoGrid
    model={Named}
    //     ^: Error: 'Named' only refers to a type, but is being used as a value here.ts(2693)
    visibleColumns={[Named["name"]]}
    //               ^: Error: same as above
  />;
}
```

For this we need some value that describes the type and has equivalent structure. This is what the models are.

Let us assume that Hilla provides an additional `NamedModel` object for our use case, which simplified structure is:

```typescript
const NamedModel = {
  name: StringModel,
};
```

Now you can use both `NamedModel` and `NamedModel.name` values as descriptions of their respective types:

```tsx
function MyView() {
  return <AutoGrid
    model={NamedModel}
    visibleColumns={[NamedModel.name]}
  />;
}
```

The key difference here from the existing class-based Hilla models is that you can directly reference a property (`NamedModel.name`), which is not supported with the current class-bases design.

### Creating models

Hilla generates both interfaces and their models for Java entities in endpoints, but sometimes you may not have a Java type. One common example is coming up with a test model for testing a frontend component integrated with Hilla models.

As a base, Hilla provides builtin model values for primitives (`BooleanModel`, `NumberModel`, `StringModel`), and empty objects (`ObjectModel`).

You can create an object model using the Hilla-provided `m.from` builder API.

Models of optional value (`T | undefined`) and array models (`T[]`) can be created by calling `m.optional(model)` and `m.array(model)`.

```typescript
// Simple objects from built-in models

interface OrderRow {
  product: string;
  qty: number;
}

const OrderRowModel = m
  .from(
    ObjectModel, // The base 
    toObject<OrderRow> // The underlying type
  ) 
  .name('OrderRow') // A string name for debug output
  .property('product', () => StringModel)
  .property('number', () => NumberModel)
  .build();

// Inheritance

interface Address extends Named {
  street: string;
}

const AddressModel = m
  .from(NamedModel, toObject<Address>)
  .name('Address')
  .property('street', () => StringModel)
  .build();

// Composition

interface Customer extends Named {
  address: Address;
}

const CustomerModel = m
  .from(NamedModel, toObject<Customer>)
  .name('Customer')
  .property('address', () => AddressModel)
  .build();

// Optionals and arrays

interface Order {
  notes?: string;
  rows: OrderRow[];
}

const OrderModel = m
  .from(ObjectModel, toOrder<Order>)
  .name('Order')
  .property('notes', () => m.optional(StringModel))
  .property('rows', () => m.array(OrderRow))
  .build();

// Self-reference using `this`

interface Employee extends Named {
  supervisor?: Employee;
}

const EmployeeModel = m
  .from(NamedModel, toObject<Employee>)
  .name('Employee')
  .property('supervisor', function() {
    // `this` refers to the resulting model object (EmployeeModel)
    return m.optional(this);
  })
  .build();
```

### Hierarchy location

When you define a property using some model as a value, the builder internally “attaches” it. The resulting property will hold a copy of the given model value with altered metadata, so that the information about the container and the key.

In the end, every model value could tell about its location in the hierarchy: it is either detached (by default) or attached some object property or array item.

In above examples, `AddressModel` and `CustomerModel.address` describe the same type (`Address` object), but their values are different: the latter is attached by nesting in the `"address"` property of `CustomerModel`. The same is also true for the deep nested models (`AddressModel.street` and `CustomerModel.address.street`).

## Reference

Now let us take a look at the model object internals.

### Base types and values

First, there is a default container for all the detached models:

```typescript
/**
 * The model hierarchy root type
 */
export interface ModelOwner {
  model?: IModel;
}

/**
 * The defaut container (hierarchy root) for detached models
 */
export const detachedModelOwner: ModelOwner = {
  model: undefined,
};
```

All the models implement the base interface `IModel<T>`:

```typescript
/**
 * The base interface for Hilla data models
 */
export interface IModel<T = unknown> {
  /**
   * String name for debug output
   */
  readonly [_name]: string;

  /**
   * Contaniner model or hierarchy root
   */
  readonly [_owner]: IModel | ModelOwner;
  
  /**
   * The key in the container (property name for object, or index number for arrays).
   */
  readonly [_key]: keyof any;

  /**
   * Value getter and type marker
   */
  readonly [_value]: T;

  /**
   * Optional marker
   */
  readonly [_optional]: boolean;

  /**
   * Other metadata (validation rules, JVM type and annotations, etc) in JSON-like structure
   */
  readonly [_meta]: ModelMetadata;
}
```

There is `AbstractModel` value that describes `unknown` type following the above interface.

### Readonly pattern

The model properties are strictly read-only to reject mutation attempts, that are likekly to cause errors down the road. Users are expected to create copies or wrap existing models using the Hilla model APIs.

### Internal and public properties

To avoid naming conflicts with the user's entity properties, the internal properties are defined using symbols, as illustrated in `IModel<T>`. 

String properties only occur in object models.

The internal properties are non-enumerable, whereas object properties can be enumerated using the regular JS workflow: `for...in` loop or `Object.keys()` / `Object.entries()` in combination with `Object.getPrototypeOf()` for hierarchy traversal.

### Object implementation notes

The `m` model APIs use `Object.create` internally to retain the hierarchy in the prototype chain. Here is a rough illustration of the implementation:

```typescript
const AddressModel: = Object.create(
  NamedModel,
  {
    // String properties that follow the structure of the underlying type (Named)
    name: {
      enumerable: true,
      get() {
        return StringModel;
      },
    },

    // Internal link to some value of the described type
    get [_value]() {
        return { name: '' };
    },
  },
}) as TypeModel<Named>;
```