import m, {
  BooleanModel,
  Future,
  Model,
  NotBlank,
  NotNull,
  NumberModel,
  ObjectModel,
  Pattern,
  StringModel,
  Size,
} from '@vaadin/hilla-models';

export interface IdEntity {
  idString: string;
}

export const IdEntityModel = m.object<IdEntity>('IdEntity').property('idString', StringModel).build();
export type IdEntityModel = typeof IdEntityModel;

export interface Product extends IdEntity {
  description: string;
  price: number;
  isInStock: boolean;
}

export const ProductModel = m
  .extend(IdEntityModel)
  .object<Product>('Product')
  .property('description', m.constrained(StringModel, NotBlank()))
  .property('price', NumberModel)
  .property('isInStock', BooleanModel)
  .build();
export type ProductModel = typeof ProductModel;

export interface Customer extends IdEntity {
  fullName: string;
  nickName: string;
}

export const CustomerModel = m
  .extend(IdEntityModel)
  .object<Customer>('Customer')
  .property('fullName', m.constrained(StringModel, NotBlank(), Size({ min: 4 })))
  .property('nickName', m.constrained(StringModel, Pattern('....*')))
  .build();
export type CustomerModel = typeof CustomerModel;

export interface Order extends IdEntity {
  customer: Customer;
  dateStart?: string;
  dateEnd?: string;
  notes: string;
  products: Product[];
  priority?: number;
  total?: number;
}

export const OrderModel = m
  .extend(IdEntityModel)
  .object<Order>('Order')
  .property('customer', m.constrained(CustomerModel, NotNull))
  .property('dateStart', m.constrained(m.optional(StringModel), Future))
  .property('dateEnd', m.constrained(m.optional(StringModel), Future))
  .property('notes', m.constrained(StringModel, NotBlank()))
  .property('priority', m.optional(NumberModel))
  .property('products', m.array(ProductModel))
  .property('total', m.optional(NumberModel))
  .build();
export type OrderModel = typeof OrderModel;

export enum RecordStatus {
  CREATED = 'CREATED',
  UPDATED = 'UPDATED',
  REMOVED = 'REMOVED',
}

export const RecordStatusModel = m.enum(RecordStatus, 'RecordStatus');
export type RecordStatusModel = typeof RecordStatusModel;

export interface TestEntity {
  fieldString: string;
  fieldOptionalString?: string;
  fieldNumber?: number;
  fieldBoolean: boolean;
  fieldObject: Record<string, unknown>;
  fieldArrayString: string[];
  fieldArrayModel: IdEntity[];
  fieldMatrixNumber: number[][];
  fieldEnum: RecordStatus;
  fieldUnknown: unknown;
  fieldAny: any;
}

export const TestModel = m
  .object<TestEntity>('TestEntity')
  .property('fieldString', StringModel)
  .property('fieldOptionalString', m.optional(StringModel))
  .property('fieldNumber', m.optional(NumberModel))
  .property('fieldBoolean', BooleanModel)
  .property('fieldObject', ObjectModel)
  .property('fieldArrayString', m.array(StringModel))
  .property('fieldArrayModel', m.array(IdEntityModel))
  .property('fieldMatrixNumber', m.array(m.array(NumberModel)))
  .property('fieldEnum', RecordStatusModel)
  .property('fieldUnknown', Model)
  .property('fieldAny', Model)
  .build();
export type TestModel = typeof TestModel;

export interface Employee extends IdEntity {
  fullName: string;
  supervisor?: Employee;
  colleagues?: Employee[];
}

export const EmployeeModel = m
  .extend(IdEntityModel)
  .object<Employee>('Employee')
  .property('fullName', StringModel)
  .property('supervisor', m.optional)
  .property('colleagues', m.array)
  .build();
export type EmployeeModel = typeof EmployeeModel;

export interface TestMessageInterpolationEntity {
  stringMinSize: string;
  stringNotBlank: string;
}

export const TestMessageInterpolationModel = m
  .object<TestMessageInterpolationEntity>('TestMessageInterpolationEntity')
  .property('stringMinSize', m.constrained(StringModel, Size({ min: 4 }), NotBlank()))
  .property('stringNotBlank', m.constrained(StringModel, NotBlank()))
  .build();
export type TestMessageInterpolationModel = typeof TestMessageInterpolationModel;

export interface WithPossibleCharList {
  charList?: string;
}

export const WithPossibleCharListModel = m
  .object<WithPossibleCharList>('WithPossibleCharList')
  .property('charList', m.optional(StringModel))
  .build();
export type WithPossibleCharListModel = typeof WithPossibleCharListModel;

export interface Level4 {
  name4?: string;
}

export const Level4Model = m.object<Level4>('Level4').property('name4', m.optional(StringModel)).build();
export type Level4Model = typeof Level4Model;

export interface Level3 {
  level4?: Level4;
  name3?: string;
}

export const Level3Model = m
  .object<Level3>('Level3')
  .property('level4', m.optional(Level4Model))
  .property('name3', m.optional(StringModel))
  .build();
export type Level3Model = typeof Level3Model;

export interface Level2 {
  level3?: Level3;
  name2?: string;
}

export const Level2Model = m
  .object<Level2>('Level2')
  .property('level3', m.optional(Level3Model))
  .property('name2', m.optional(StringModel))
  .build();
export type Level2Model = typeof Level2Model;

export interface Level1 {
  anotherLevel2?: Array<string | undefined>;
  level2?: Array<Level2 | undefined>;
  name1?: string;
}

export const Level1Model = m
  .object<Level1>('Level1')
  .property('anotherLevel2', m.optional(m.array(m.optional(StringModel))))
  .property('level2', m.optional(m.array(Level2Model)))
  .property('name1', m.optional(StringModel))
  .build();
export type Level1Model = typeof Level1Model;
