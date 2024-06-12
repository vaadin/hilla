import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import { StringModel, BooleanModel, NumberModel } from '../src/core.js';
import { m } from '../src/m.js';
import { $meta, $name, type ModelMetadata, Model, type ExtendedModel } from '../src/model.js';

use(chaiLike);

describe('ModelBuilder', () => {
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

  let NamedModel: ExtendedModel<Named>;
  let AddressModel: ExtendedModel<Address>;

  beforeEach(() => {
    NamedModel = m.object<Named>('Named').property('name', StringModel).build();
    AddressModel = m
      .extend<Address>(NamedModel)
      .name('Address')
      .property('street', StringModel)
      .property('premise', StringModel)
      .property('city', StringModel)
      .property('postalCode', StringModel)
      .property('country', StringModel)
      .build();
  });

  it('should create a simple model', () => {
    const meta = { jvmType: 'com.example.application.Named' };

    expect(NamedModel).to.exist;
    expect(NamedModel).to.be.instanceof(Model);
    expect(NamedModel).to.have.property($name, 'Named');
    expect(NamedModel).to.have.property($meta).which.is.like(meta);
    expect(NamedModel).to.have.property('name');
    expect(NamedModel).to.have.property('name').which.is.instanceof(Model);
  });

  it('should allow model inheritance', () => {
    expect(AddressModel).to.be.instanceof(NamedModel);
    expect(AddressModel).to.have.property($name, 'Address');
    expect(AddressModel).to.have.property('name').which.is.instanceof(StringModel);
    expect(AddressModel).to.have.property('street').which.is.instanceof(StringModel);
    expect(AddressModel).to.have.property('premise').which.is.instanceof(StringModel);
    expect(AddressModel).to.have.property('city').which.is.instanceof(StringModel);
    expect(AddressModel).to.have.property('postalCode').which.is.instanceof(StringModel);
    expect(AddressModel).to.have.property('country').which.is.instanceof(StringModel);
  });

  it('should allow model composition', () => {
    const CustomerModel = m
      .object<Customer>('Customer')
      .property('name', StringModel)
      .property('subscriptionActive', BooleanModel)
      .property('subscriptionTier', NumberModel)
      .property('address', AddressModel)
      .build();

    expect(CustomerModel).to.have.property('name').which.is.instanceof(StringModel);
    expect(CustomerModel).to.have.property('subscriptionActive').which.is.instanceof(NumberModel);
    expect(CustomerModel).to.have.property('subscriptionTier').which.is.instanceof(BooleanModel);
    expect(CustomerModel).to.have.property('address').which.is.instanceof(AddressModel);
  });

  it('should allow self-reference', () => {
    const EmployeeModel = m.extend<Employee>(NamedModel).name('Employee').property('supervisor', m.optional).build();
  });
});
