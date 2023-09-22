/* eslint-disable @typescript-eslint/no-use-before-define, @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-unnecessary-type-assertion, @typescript-eslint/no-shadow */
// API to test
import {
  _enum,
  _getPropertyModel,
  ArrayModel,
  BooleanModel,
  EnumModel,
  makeEnumEmptyValueCreator,
  makeObjectEmptyValueCreator,
  NotBlank,
  NumberModel,
  ObjectModel,
  Pattern,
  Positive,
  Required,
  Size,
  StringModel,
} from '../src/index.js';

export interface IdEntity {
  idString: string;
}
export class IdEntityModel<T extends IdEntity = IdEntity> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(IdEntityModel);

  get idString(): StringModel {
    return this[_getPropertyModel]('idString', (parent, key) => new StringModel(parent, key, false));
  }
}

export interface Product extends IdEntity {
  description: string;
  price: number;
  isInStock: boolean;
}
export class ProductModel<T extends Product = Product> extends IdEntityModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(ProductModel);

  get description(): StringModel {
    return this[_getPropertyModel]('description', (parent, key) => new StringModel(parent, key, false, new Required()));
  }

  get price(): NumberModel {
    return this[_getPropertyModel]('price', (parent, key) => new NumberModel(parent, key, false, new Positive()));
  }

  get isInStock(): BooleanModel {
    return this[_getPropertyModel]('isInStock', (parent, key) => new BooleanModel(parent, key, false));
  }
}

export interface Customer extends IdEntity {
  fullName: string;
  nickName: string;
}
export class CustomerModel<T extends Customer = Customer> extends IdEntityModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(CustomerModel);

  get fullName(): StringModel {
    return this[_getPropertyModel](
      'fullName',
      (parent, key) => new StringModel(parent, key, false, new Size({ min: 4 }), new Required()),
    );
  }

  get nickName(): StringModel {
    return this[_getPropertyModel](
      'nickName',
      (parent, key) => new StringModel(parent, key, false, new Pattern('....*')),
    );
  }
}

export interface Order extends IdEntity {
  customer: Customer;
  notes: string;
  priority: number;
  products: readonly Product[];
  total?: number;
}
export class OrderModel<T extends Order = Order> extends IdEntityModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(OrderModel);

  get customer(): CustomerModel {
    return this[_getPropertyModel]('customer', (parent, key) => new CustomerModel(parent, key, false, new Required()));
  }

  get notes(): StringModel {
    return this[_getPropertyModel]('notes', (parent, key) => new StringModel(parent, key, false, new Required()));
  }

  get priority(): NumberModel {
    return this[_getPropertyModel]('priority', (parent, key) => new NumberModel(parent, key, false));
  }

  get products(): ArrayModel<ProductModel> {
    return this[_getPropertyModel](
      'products',
      (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new ProductModel(parent, key, false)),
    );
  }

  get total(): NumberModel {
    return this[_getPropertyModel]('total', (parent, key) => new NumberModel(parent, key, true));
  }
}

export interface TestEntity {
  fieldString: string;
  fieldNumber: number;
  fieldBoolean: boolean;
  fieldObject: Record<string, unknown>;
  fieldArrayString: readonly string[];
  fieldArrayModel: readonly IdEntity[];
  fieldMatrixNumber: ReadonlyArray<readonly number[]>;
  fieldEnum: RecordStatus;
  fieldAny: any;
}

export class TestModel<T extends TestEntity = TestEntity> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(TestModel);

  get fieldString(): StringModel {
    return this[_getPropertyModel]('fieldString', (parent, key) => new StringModel(parent, key, false));
  }

  get fieldNumber(): NumberModel {
    return this[_getPropertyModel]('fieldNumber', (parent, key) => new NumberModel(parent, key, false));
  }

  get fieldBoolean(): BooleanModel {
    return this[_getPropertyModel]('fieldBoolean', (parent, key) => new BooleanModel(parent, key, false));
  }

  get fieldObject(): ObjectModel<Record<string, unknown>> {
    return this[_getPropertyModel]('fieldObject', (parent, key) => new ObjectModel(parent, key, false));
  }

  get fieldArrayString(): ArrayModel<StringModel> {
    return this[_getPropertyModel](
      'fieldArrayString',
      (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, false)),
    );
  }

  get fieldArrayModel(): ArrayModel<IdEntityModel> {
    return this[_getPropertyModel](
      'fieldArrayModel',
      (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new IdEntityModel(parent, key, false)),
    );
  }

  get fieldMatrixNumber(): ArrayModel<ArrayModel<NumberModel>> {
    return this[_getPropertyModel](
      'fieldMatrixNumber',
      (parent, key) =>
        new ArrayModel(
          parent,
          key,
          false,
          (parent, key) =>
            new ArrayModel(parent, key, false, (parent, key) => new NumberModel(parent, key, false, new Positive())),
        ),
    );
  }

  get fieldEnum(): RecordStatusModel {
    return this[_getPropertyModel]('fieldEnum', (parent, key) => new RecordStatusModel(parent, key, false));
  }

  get fieldAny(): ObjectModel {
    return this[_getPropertyModel]('fieldAny', (parent, key) => new ObjectModel(parent, key, false));
  }
}

export interface Employee extends IdEntity {
  fullName: string;
  supervisor?: Employee;
  colleagues?: Employee[];
}
export class EmployeeModel<T extends Employee = Employee> extends IdEntityModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(EmployeeModel);

  get fullName(): StringModel {
    return this[_getPropertyModel]('fullName', (parent, key) => new StringModel(parent, key, false));
  }

  get supervisor(): EmployeeModel {
    return this[_getPropertyModel]('supervisor', (parent, key) => new EmployeeModel(parent, key, true));
  }

  get colleagues(): ArrayModel<EmployeeModel> {
    return this[_getPropertyModel](
      'colleagues',
      (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new EmployeeModel(parent, key, false)),
    );
  }
}

export interface TestMessageInterpolationEntity {
  stringMinSize: string;
  stringNotBlank: string;
}
export class TestMessageInterpolationModel<
  T extends TestMessageInterpolationEntity = TestMessageInterpolationEntity,
> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(TestMessageInterpolationModel);

  get stringMinSize(): StringModel {
    return this[_getPropertyModel](
      'stringMinSize',
      (parent, key) => new StringModel(parent, key, false, new Size({ min: 4 }), new Required()),
    );
  }

  get stringNotBlank(): StringModel {
    return this[_getPropertyModel](
      'stringNotBlank',
      (parent, key) => new StringModel(parent, key, false, new NotBlank(), new Required()),
    );
  }
}

export enum RecordStatus {
  CREATED = 'CREATED',
  UPDATED = 'UPDATED',
  REMOVED = 'REMOVED',
}

export class RecordStatusModel extends EnumModel<typeof RecordStatus> {
  static override createEmptyValue = makeEnumEmptyValueCreator(RecordStatusModel);

  readonly [_enum] = RecordStatus;
}

export interface WithPossibleCharList {
  charList?: string;
}

export class WithPossibleCharListModel extends ObjectModel<WithPossibleCharList> {
  static override createEmptyValue = makeObjectEmptyValueCreator(WithPossibleCharListModel);

  get charList(): StringModel {
    return this[_getPropertyModel]('charList', (parent, key) => new StringModel(parent, key, true));
  }
}
