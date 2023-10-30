import { expect } from 'chai';
import { CoreModelBuilder } from './CoreModelBuilder.js';
import { AbstractModel, _optional, type IModel, type Value, _value } from './Model.js';
import { isModel } from './utils.js';

export const PrimitiveModel = CoreModelBuilder.from(AbstractModel, () => undefined as unknown)
  .name('primitive')
  .build();

export const StringModel = CoreModelBuilder.from(PrimitiveModel, () => '')
  .name('string')
  .build();

export const NumberModel = CoreModelBuilder.from(PrimitiveModel, () => 0)
  .name('number')
  .build();

export const BooleanModel = CoreModelBuilder.from(PrimitiveModel, () => false)
  .name('boolean')
  .build();

// Optional

export interface IOptionalModel<T> extends IModel<T | undefined> {
  [_optional]: true;
}

export type OptionalModel<M extends IModel> = IOptionalModel<Value<M>> & Omit<M, typeof _value>;

// Object

export const emptyObject: object = {};

export const ObjectModel = CoreModelBuilder.from(AbstractModel, () => emptyObject)
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

export interface IArrayModel<MItem extends IModel = IModel> extends IModel<Array<Value<MItem>>> {
  [_itemModel]: MItem;
}

export const ArrayModel: IArrayModel = CoreModelBuilder.from(AbstractModel, () => [] as unknown[])
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

export const EnumModel = CoreModelBuilder.from(AbstractModel)
  .define(_enum, {} as Enum)
  .build();

// Union

export const _members = Symbol('members');

export interface IUnionModel<MM extends readonly [IModel, ...IModel[]]> extends IModel<Value<MM[number]>> {
  [_members]: MM;
}

// All together now

export type TypeModel<T> = IModel<T> &
  (T extends typeof Enum
    ? IEnumModel<T>
    : T extends Array<infer I>
    ? IArrayModel<TypeModel<I>>
    : T extends object
    ? {
        readonly [Key in string & keyof T]-?: TypeModel<T[Key]>;
      }
    : undefined extends T
    ? OptionalModel<TypeModel<Exclude<T, undefined>>>
    : unknown);
