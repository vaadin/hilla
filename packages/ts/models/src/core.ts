import type { EmptyObject } from 'type-fest';
import { CoreModelBuilder } from './builders.js';
import { $enum, $itemModel, type $members, type AnyObject, type Enum, Model, type Value } from './model.js';

/**
 * The model of a primitive value, like `string`, `number` or `boolean`.
 */
export type PrimitiveModel<V = unknown> = Model<V>;
export const PrimitiveModel = new CoreModelBuilder(Model, (): unknown => undefined).name('primitive').build();

/**
 * The model of a string value.
 */
export type StringModel = PrimitiveModel<string>;
export const StringModel = new CoreModelBuilder(PrimitiveModel, () => '').name('string').build();

/**
 * The model of a number value.
 */
export type NumberModel = PrimitiveModel<number>;
export const NumberModel = new CoreModelBuilder(PrimitiveModel, () => 0).name('number').build();

/**
 * The model of a boolean value.
 */
export type BooleanModel = PrimitiveModel<boolean>;
export const BooleanModel = new CoreModelBuilder(PrimitiveModel, () => false).name('boolean').build();

/**
 * The model of an array data.
 */
export type ArrayModel<M extends Model = Model> = Model<
  Array<Value<M>>,
  Readonly<{
    [$itemModel]: M;
  }>
>;

export const ArrayModel = new CoreModelBuilder(Model, (): unknown[] => [])
  .name('Array')
  .define($itemModel, { value: Model })
  .build();

/**
 * The model of an object data.
 */
export type ObjectModel<V, EX extends AnyObject = EmptyObject, R extends keyof any = never> = Model<V, EX, R>;

export const ObjectModel = new CoreModelBuilder(Model, (): AnyObject => ({})).name('Object').build();

/**
 * The model of an enum data.
 */
export type EnumModel<T extends typeof Enum> = Model<
  T[keyof T],
  Readonly<{
    [$enum]: T;
  }>
>;

export const EnumModel = new CoreModelBuilder<(typeof Enum)[keyof typeof Enum]>(Model)
  .name('Enum')
  // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
  .define($enum, { value: {} as typeof Enum })
  .defaultValueProvider((self) => Object.values(self[$enum])[0])
  .build();

/**
 * The model of a union data.
 */
export type UnionModel<MM extends Model[]> = Model<Value<MM[number]>, Readonly<{ [$members]: MM }>>;
