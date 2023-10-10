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
    return this[_getPropertyModel](
      'description',
      (parent, key) => new StringModel(parent, key, false, { validators: [new Required()] }),
    );
  }

  get price(): NumberModel {
    return this[_getPropertyModel](
      'price',
      (parent, key) => new NumberModel(parent, key, false, { validators: [new Positive()] }),
    );
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
      (parent, key) => new StringModel(parent, key, false, { validators: [new Size({ min: 4 }), new Required()] }),
    );
  }

  get nickName(): StringModel {
    return this[_getPropertyModel](
      'nickName',
      (parent, key) => new StringModel(parent, key, false, { validators: [new Pattern('....*')] }),
    );
  }
}

export interface Order extends IdEntity {
  customer: Customer;
  notes: string;
  priority: number;
  products: Product[];
  total?: number;
}

export class OrderModel<T extends Order = Order> extends IdEntityModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(OrderModel);

  get customer(): CustomerModel {
    return this[_getPropertyModel](
      'customer',
      (parent, key) => new CustomerModel(parent, key, false, { validators: [new Required()] }),
    );
  }

  get notes(): StringModel {
    return this[_getPropertyModel](
      'notes',
      (parent, key) => new StringModel(parent, key, false, { validators: [new Required()] }),
    );
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
  fieldArrayString: string[];
  fieldArrayModel: IdEntity[];
  fieldMatrixNumber: number[][];
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
            new ArrayModel(
              parent,
              key,
              false,
              (parent, key) => new NumberModel(parent, key, false, { validators: [new Positive()] }),
            ),
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
      (parent, key) => new StringModel(parent, key, false, { validators: [new Size({ min: 4 }), new Required()] }),
    );
  }

  get stringNotBlank(): StringModel {
    return this[_getPropertyModel](
      'stringNotBlank',
      (parent, key) => new StringModel(parent, key, false, { validators: [new NotBlank(), new Required()] }),
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

export interface Level4 {
  name4?: string;
}

export interface Level3 {
  level4?: Level4;
  name3?: string;
}

export interface Level2 {
  level3?: Level3;
  name2?: string;
}

export interface Level1 {
  anotherLevel2?: Array<string | undefined>;
  level2?: Array<Level2 | undefined>;
  name1?: string;
}

export class Level4Model<T extends Level4 = Level4> extends ObjectModel<T> {
  declare static createEmptyValue: () => Level4;
  get name4(): StringModel {
    return this[_getPropertyModel]('name4', (parent, key) => new StringModel(parent, key, true));
  }
}

export class Level3Model<T extends Level3 = Level3> extends ObjectModel<T> {
  declare static createEmptyValue: () => Level3;
  get level4(): Level4Model {
    return this[_getPropertyModel]('level4', (parent, key) => new Level4Model(parent, key, true));
  }

  get name3(): StringModel {
    return this[_getPropertyModel]('name3', (parent, key) => new StringModel(parent, key, true));
  }
}

export class Level2Model<T extends Level2 = Level2> extends ObjectModel<T> {
  declare static createEmptyValue: () => Level2;
  get level3(): Level3Model {
    return this[_getPropertyModel]('level3', (parent, key) => new Level3Model(parent, key, true));
  }

  get name2(): StringModel {
    return this[_getPropertyModel]('name2', (parent, key) => new StringModel(parent, key, true));
  }
}

export class Level1Model<T extends Level1 = Level1> extends ObjectModel<T> {
  declare static createEmptyValue: () => Level1;

  get anotherLevel2(): ArrayModel<StringModel> {
    return this[_getPropertyModel](
      'anotherLevel2',
      (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true)),
    );
  }

  get level2(): ArrayModel<Level2Model> {
    return this[_getPropertyModel](
      'level2',
      (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new Level2Model(parent, key, true)),
    );
  }

  get name1(): StringModel {
    return this[_getPropertyModel]('name1', (parent, key) => new StringModel(parent, key, true));
  }
}
