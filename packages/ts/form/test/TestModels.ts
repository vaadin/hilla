/* tslint:disable:max-classes-per-file */
// API to test
import {
  _getPropertyModel,
  ArrayModel,
  BooleanModel,
  ModelConstructor,
  NotBlank,
  NumberModel,
  ObjectModel,
  Pattern,
  Positive,
  Required,
  Size,
  StringModel,
} from '../src';

export interface IdEntity {
  idString: string;
}
export class IdEntityModel<T extends IdEntity = IdEntity> extends ObjectModel<T> {
  public static override createEmptyValue: () => IdEntity;

  public get idString(): StringModel {
    return this[_getPropertyModel]('idString', StringModel, [false]);
  }
}

export interface Product extends IdEntity {
  description: string;
  price: number;
  isInStock: boolean;
}
export class ProductModel<T extends Product = Product> extends IdEntityModel<T> {
  public static override createEmptyValue: () => Product;

  public get description() {
    return this[_getPropertyModel]('description', StringModel, [false, new Required()]);
  }

  public get price() {
    return this[_getPropertyModel]('price', NumberModel, [false, new Positive()]);
  }

  public get isInStock() {
    return this[_getPropertyModel]('isInStock', BooleanModel, [false]);
  }
}

export interface Customer extends IdEntity {
  fullName: string;
  nickName: string;
}
export class CustomerModel<T extends Customer = Customer> extends IdEntityModel<T> {
  public static override createEmptyValue: () => Customer;

  public get fullName() {
    return this[_getPropertyModel]('fullName', StringModel, [
      false,
      new Size({ min: 4 }),
      new Required(),
    ]) as StringModel;
  }

  public get nickName() {
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
  public static override createEmptyValue: () => Order;

  public get customer(): CustomerModel {
    return this[_getPropertyModel]('customer', CustomerModel, [false, new Required()]);
  }

  public get notes(): StringModel {
    return this[_getPropertyModel]('notes', StringModel, [false, new Required()]);
  }

  public get priority(): NumberModel {
    return this[_getPropertyModel]('priority', NumberModel, [false]);
  }

  public get products(): ArrayModel<Product, ProductModel> {
    return this[_getPropertyModel](
      'products',
      ArrayModel as ModelConstructor<ReadonlyArray<Product>, ArrayModel<Product, ProductModel>>,
      [false, ProductModel, [false]],
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
  public static override createEmptyValue: () => TestEntity;

  public get fieldString() {
    return this[_getPropertyModel]('fieldString', StringModel, [false]) as StringModel;
  }

  public get fieldNumber() {
    return this[_getPropertyModel]('fieldNumber', NumberModel, [false]) as NumberModel;
  }

  public get fieldBoolean() {
    return this[_getPropertyModel]('fieldBoolean', BooleanModel, [false]) as BooleanModel;
  }

  public get fieldObject() {
    return this[_getPropertyModel]('fieldObject', ObjectModel, [false]) as ObjectModel<Record<string, unknown>>;
  }

  public get fieldArrayString() {
    return this[_getPropertyModel]('fieldArrayString', ArrayModel, [false, StringModel, [false]]) as ArrayModel<
      string,
      StringModel
    >;
  }

  public get fieldArrayModel() {
    return this[_getPropertyModel]('fieldArrayModel', ArrayModel, [false, IdEntityModel, [false]]) as ArrayModel<
      IdEntity,
      IdEntityModel
    >;
  }

  public get fieldMatrixNumber() {
    return this[_getPropertyModel]('fieldMatrixNumber', ArrayModel, [
      false,
      ArrayModel,
      [false, NumberModel, [false, new Positive()]],
    ]) as ArrayModel<ReadonlyArray<number>, ArrayModel<number, NumberModel>>;
  }

  public get fieldAny() {
    return this[_getPropertyModel]('fieldAny', ObjectModel, [false]) as ObjectModel<any>;
  }
}

export interface Employee extends IdEntity {
  fullName: string;
  supervisor?: Employee;
  colleagues?: Employee[];
}
export class EmployeeModel<T extends Employee = Employee> extends IdEntityModel<T> {
  public static override createEmptyValue: () => Employee;

  public get fullName() {
    return this[_getPropertyModel]('fullName', StringModel, [false]) as StringModel;
  }

  public get supervisor(): EmployeeModel {
    return this[_getPropertyModel]('supervisor', EmployeeModel, [true]);
  }

  public get colleagues() {
    return this[_getPropertyModel]('colleagues', ArrayModel, [true, EmployeeModel, [false]]);
  }
}

export interface TestMessageInterpolationEntity {
  stringMinSize: string;
  stringNotBlank: string;
}
export class TestMessageInterpolationModel<
  T extends TestMessageInterpolationEntity = TestMessageInterpolationEntity,
> extends ObjectModel<T> {
  public static override createEmptyValue: () => TestMessageInterpolationEntity;

  public get stringMinSize() {
    return this[_getPropertyModel]('stringMinSize', StringModel, [
      false,
      new Size({ min: 4 }),
      new Required(),
    ]) as StringModel;
  }

  public get stringNotBlank() {
    return this[_getPropertyModel]('stringNotBlank', StringModel, [
      false,
      new NotBlank(),
      new Required(),
    ]) as StringModel;
  }
}
