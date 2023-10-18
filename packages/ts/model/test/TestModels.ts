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
} from '../src';

interface Named {
  name: string;
}

interface Address extends Named {
  street: string;
  premise: string;
  city: string;
  postalCode: string;
  country?: string;
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
  .property('postalcode', StringModel)
  .property('country', m.optional(StringModel))
  .build();

getValue(AddressModel).name;
getValue(AddressModel).postalCode;
getValue(AddressModel.country)?.length;

// Composition

const CustomerModel = m
  .from(NamedModel, toObject<Customer>)
  .name('Customer')
  .property('subscriptionActive', BooleanModel)
  .property('subscriptionTier', NumberModel)
  .property('address', AddressModel)
  .build();

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
console.log('Empty value: CustomerModel', CustomerModel[_value]);

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
