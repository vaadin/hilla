/* tslint:disable:max-classes-per-file */
// API to test
import {
  _getPropertyModel,
  ArrayModel,
  BooleanModel,
  ModelConstructor,
  NumberModel,
  ObjectModel,
  Pattern,
  Positive,
  Required,
  Size,
  StringModel,
} from '../../src';

export interface IdEntity {
  idString: string;
}
export class IdEntityModel<T extends IdEntity = IdEntity> extends ObjectModel<T> {
  static createEmptyValue: () => IdEntity;

  get idString(): StringModel {
    return this[_getPropertyModel]('idString', StringModel, [false]);
  }
}

export interface Product extends IdEntity {
  description: string;
  price: number;
  isInStock: boolean;
}
export class ProductModel<T extends Product = Product> extends IdEntityModel<T> {
  static createEmptyValue: () => Product;

  get description() {
    return this[_getPropertyModel]('description', StringModel, [false, new Required()]);
  }

  get price() {
    return this[_getPropertyModel]('price', NumberModel, [false, new Positive()]);
  }

  get isInStock() {
    return this[_getPropertyModel]('isInStock', BooleanModel, [false]);
  }
}

interface Customer extends IdEntity {
  fullName: string;
  nickName: string;
}
export class CustomerModel<T extends Customer = Customer> extends IdEntityModel<T> {
  static createEmptyValue: () => Customer;

  get fullName() {
    return this[_getPropertyModel]('fullName', StringModel, [
      false,
      new Size({ min: 4 }),
      new Required(),
    ]) as StringModel;
  }

  get nickName() {
    return this[_getPropertyModel]('nickName', StringModel, [false, new Pattern('....*')]) as StringModel;
  }
}

export interface Order extends IdEntity {
  customer: Customer;
  notes: string;
  priority: number;
  products: ReadonlyArray<Product>;
}
export class OrderModel<T extends Order = Order> extends IdEntityModel<T> {
  static createEmptyValue: () => Order;

  get customer(): CustomerModel {
    return this[_getPropertyModel]('customer', CustomerModel, [false, new Required()]);
  }

  get notes(): StringModel {
    return this[_getPropertyModel]('notes', StringModel, [false, new Required()]);
  }

  get priority(): NumberModel {
    return this[_getPropertyModel]('priority', NumberModel, [false]);
  }

  get products(): ArrayModel<Product, ProductModel> {
    return this[_getPropertyModel](
      'products',
      ArrayModel as ModelConstructor<ReadonlyArray<Product>, ArrayModel<Product, ProductModel>>,
      [false, ProductModel, [false]]
    );
  }
}

export interface TestEntity {
  fieldString: string;
  fieldNumber: number;
  fieldBoolean: boolean;
  fieldObject: Record<string, unknown>;
  fieldArrayString: string[];
  fieldArrayModel: IdEntity[];
  fieldMatrixNumber: number[][];
  fieldAny: any;
}
export class TestModel<T extends TestEntity = TestEntity> extends ObjectModel<T> {
  static createEmptyValue: () => TestEntity;

  get fieldString() {
    return this[_getPropertyModel]('fieldString', StringModel, [false]) as StringModel;
  }

  get fieldNumber() {
    return this[_getPropertyModel]('fieldNumber', NumberModel, [false]) as NumberModel;
  }

  get fieldBoolean() {
    return this[_getPropertyModel]('fieldBoolean', BooleanModel, [false]) as BooleanModel;
  }

  get fieldObject() {
    return this[_getPropertyModel]('fieldObject', ObjectModel, [false]) as ObjectModel<Record<string, unknown>>;
  }

  get fieldArrayString() {
    return this[_getPropertyModel]('fieldArrayString', ArrayModel, [false, StringModel, [false]]) as ArrayModel<
      string,
      StringModel
    >;
  }

  get fieldArrayModel() {
    return this[_getPropertyModel]('fieldArrayModel', ArrayModel, [false, IdEntityModel, [false]]) as ArrayModel<
      IdEntity,
      IdEntityModel
    >;
  }

  get fieldMatrixNumber() {
    return this[_getPropertyModel]('fieldMatrixNumber', ArrayModel, [
      false,
      ArrayModel,
      [false, NumberModel, [false, new Positive()]],
    ]) as ArrayModel<ReadonlyArray<number>, ArrayModel<number, NumberModel>>;
  }

  get fieldAny() {
    return this[_getPropertyModel]('fieldAny', ObjectModel, [false]) as ObjectModel<any>;
  }
}

export interface Employee extends IdEntity {
  fullName: string;
  supervisor?: Employee;
  colleagues?: Employee[];
}
export class EmployeeModel<T extends Employee = Employee> extends IdEntityModel<T> {
  static createEmptyValue: () => Employee;

  get fullName() {
    return this[_getPropertyModel]('fullName', StringModel, [false]) as StringModel;
  }

  get supervisor(): EmployeeModel {
    return this[_getPropertyModel]('supervisor', EmployeeModel, [true]);
  }

  get colleagues() {
    return this[_getPropertyModel]('colleagues', ArrayModel, [true, EmployeeModel, [false]]);
  }
}
