import { BaseModelBuilder } from './BaseModelBuilder.js';
import { AbstractModel, _optional, type IModel, type Value, _value } from './Model.js';
import { isModel } from './utils.js';

export const PrimitiveModel = BaseModelBuilder.from(AbstractModel, () => undefined as unknown)
  .name('primitive')
  .build();

export const StringModel = BaseModelBuilder.from(PrimitiveModel, () => '')
  .name('string')
  .build();

export const NumberModel = BaseModelBuilder.from(PrimitiveModel, () => 0)
  .name('number')
  .build();

export const BooleanModel = BaseModelBuilder.from(PrimitiveModel, () => false)
  .name('boolean')
  .build();

// Optional

export type IModelOfOptional<M extends IModel> = Omit<M, typeof _value> & {
  [_value]: Value<M> | undefined;
  [_optional]: true;
};

// Object

export const emptyObject: Record<never, never> = {};

export const ObjectModel = BaseModelBuilder.from(AbstractModel, () => emptyObject)
  .name('Object')
  .build();

export function toObject<T>(this: typeof ObjectModel): T {
  const obj: Record<string, unknown> = {};
  const proto = Object.getPrototypeOf(this);
  if (isModel(proto, ObjectModel)) {
    Object.assign(obj, toObject.call(proto));
  }
  const properties = this as unknown as Record<string, IModel>;
  for (const [key, value] of Object.entries(properties)) {
    if (!value[_optional]) {
      obj[key] = value[_value];
    }
  }
  return obj as T;
}

// Array

export const _itemModel = Symbol('itemModel');

export interface IArrayModel<T> extends IModel<T[]> {
  [_itemModel]: IModel<T>;
}

export const ArrayModel: IArrayModel<unknown> = BaseModelBuilder.from(AbstractModel, () => [] as unknown[])
  .name('Array')
  .define(_itemModel, AbstractModel)
  .build();

// Enum

export declare enum Enum {}

export const _enum = Symbol('enum');

export interface IEnumModel<E extends typeof Enum = typeof Enum> extends IModel<E[keyof E]> {
  [_enum]: E;
}

export function toEnum<E extends typeof Enum = typeof Enum>(this: typeof EnumModel): E[keyof E] {
  const enumObject = this[_enum];
  const firstValue = Object.values(enumObject)[0] as unknown as E[keyof E];
  return firstValue;
}

export const EnumModel = BaseModelBuilder.from(AbstractModel)
  .define(_enum, {} as Enum)
  .build();

// Union

export const _members = Symbol('members');

export interface IUnionModel<MM extends readonly [IModel, ...IModel[]]> extends IModel<Value<MM[number]>> {
  [_members]: MM;
}
