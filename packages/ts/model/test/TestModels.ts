/* eslint prefer-arrow-callback: [ "error", { "allowUnboundThis": true } ] */
/* eslint-disable @typescript-eslint/no-invalid-this */
/* eslint-disable @typescript-eslint/no-unused-expressions */
/* eslint-disable no-console */
/* eslint-disable no-restricted-syntax */

import {
  BooleanModel,
  NumberModel,
  ObjectModel,
  StringModel,
  getValue,
  m,
  toObject,
  type IModel,
  _value,
  type ModelMetadata,
  type ModelOwner,
  _name,
  _owner,
  detachedModelOwner,
  _key,
  _members,
  _meta,
  _optional,
  type TypeModel,
} from '../src';

interface Named {
  name: string;
}

interface Address extends Named {
  street: string;
  premise: string;
  city: string;
  postalCode: string;
  country: string;
}

interface Customer extends Named {
  name: string;
  subscriptionActive: boolean;
  subscriptionTier: number;
  address: Address;
}

interface OrderRow {
  product: string;
  qty: number;
}

interface Order {
  notes?: string;
  discountCodes: string[];
  rows: OrderRow[];
  customer?: Customer;
  address: Address | undefined;
}

enum AccountType {
  Active = 'Active',
  Suspended = 'Suspended',
}

interface Account extends Named {
  type: AccountType;
}

interface FooItem {
  foo: string;
}

interface BarItem {
  bar: number;
}

interface Container {
  item: BarItem | FooItem;
}

interface Employee extends Named {
  supervisor?: Employee;
}

interface Commentable {
  comments?: string[];
}

// Class based models with static properties

/*
type ModelClass<T = unknown> = IModel<T> & {
  new(...rest: any[]): {};
};

type ModelClassWithProperty<M extends ModelClass, K extends keyof any, V = unknown> = M & Readonly<Record<K, V>>;

function staticGetterMixin<M extends ModelClass, K extends keyof any, V = unknown>(superClass: ModelClass, key: K, valueGetter: (this: M) => V): ModelClassWithProperty<M, K, V> {
  return class extends superClass {
    static get[key](): V {
      return valueGetter.call(this as M);
    }
  } as ModelClassWithProperty<M, K, V>;
}

class AModel {
  static readonly [_name]: string = '';
  static readonly [_owner]: IModel<unknown> | ModelOwner = detachedModelOwner;
  static readonly [_key]: keyof any = 'model'
  static readonly [_value]: unknown = undefined;
  static readonly [_meta]: ModelMetadata = {};
  static readonly [_optional]: boolean = false;
}

class StringClassModel extends staticGetterMixin(AModel, _value, () => '') {}
class NamedClassModel extends staticGetterMixin(AModel, _value, () => ({} as Named)) {
  static override readonly name = StringClassModel;
}

class EmployeeClassModel extends NamedClassModel {
  static readonly supervisor = EmployeeClassModel;
}
*/

// Simple

const NamedModel = m
  .from(ObjectModel, toObject<Named>)
  .name('Named')
  .meta({
    jvmType: 'com.example.application.Named',
  })
  .property('name', StringModel)
  .build();

// Inheritance

const AddressModel = m
  .from(NamedModel, toObject<Address>)
  .name('Address')
  .property('street', StringModel)
  .property('premise', StringModel)
  .property('city', StringModel)
  .property('postalCode', StringModel)
  .property('country', StringModel)
  .build();

const address = getValue(AddressModel);
getValue(AddressModel).postalCode;
getValue(AddressModel.postalCode);
getValue(AddressModel.country).length;

// Composition

const CustomerModel = m
  .from(NamedModel, toObject<Customer>)
  .name('Customer')
  .property('subscriptionActive', BooleanModel)
  .property('subscriptionTier', NumberModel)
  .property('address', AddressModel)
  .build();

// Self reference

const EmployeeModel = m
  .from(NamedModel, toObject<Employee>)
  .name('Employee')
  .property('supervisor', m.optional)
  .build();

EmployeeModel.supervisor.supervisor.supervisor.name;

// Array

const OrderRowModel = m
  .from(ObjectModel, toObject<OrderRow>)
  .name('OrderRow')
  .property('product', StringModel)
  .property('qty', NumberModel)
  .build();

const OrderModel = m
  .from(ObjectModel, toObject<Order>)
  .name('Order')
  .property('notes', m.optional(StringModel))
  .property('discountCodes', m.array(StringModel))
  .property('rows', m.array(OrderRowModel))
  .property('customer', m.optional(CustomerModel))
  .property('address', m.optional(AddressModel))
  .build();

const CommentableModel: TypeModel<Commentable> = m
  .from(ObjectModel, toObject<Commentable>)
  .name('Commentable')
  .property('comments', () => {
    const array = m.array(StringModel);
    const optional = m.optional(array);
    return optional;
  })
  .build();

// Enum

const AccountModel = m
  .from(NamedModel, toObject<Account>)
  .name('Account')
  .property('type', m.enum(AccountType))
  .build();

const t: AccountType = AccountModel.type[_value];

// Union

const FooItemModel = m
  .from(ObjectModel, toObject<FooItem>)
  .name('FooItem')
  .property('foo', StringModel)
  .build();

const BarItemModel = m
  .from(ObjectModel, toObject<BarItem>)
  .name('BarItem')
  .property('bar', NumberModel)
  .build();

const ContainerModel = m
  .from(ObjectModel, toObject<Container>)
  .name('Container')
  .property('item', m.union(FooItemModel, BarItemModel))
  .build();

const containerItem: BarItem | FooItem = ContainerModel.item[_value];

console.log('\n\nCustomerModel has:\n');

for (const key in CustomerModel) {
  const value: IModel = (CustomerModel as unknown as Record<typeof key, IModel>)[key];
  console.log(key, '\n ', String(value));
}

console.log('\n\nCustomerModel.address has:\n');

for (const key in CustomerModel.address) {
  const value: IModel = (CustomerModel.address as unknown as Record<typeof key, IModel>)[key];
  console.log(key, '\n ', String(value));
}

console.log('\n');
console.log('Empty value for CustomerModel:', CustomerModel[_value]);

console.log('\n');
console.log('AddressModel.street\n', String(AddressModel.street));
console.log('-- vs --');
console.log('CustomerModel.address.street\n', String(CustomerModel.address.street));
console.log('\n');

console.log('\n\nOrderModel has:\n');

for (const key in OrderModel) {
  const value: IModel = (OrderModel as unknown as Record<typeof key, IModel>)[key];
  console.log(key, '\n ', String(value));
}
