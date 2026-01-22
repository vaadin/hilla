import {
  type $defaultValue,
  type $members,
  type AnyObject,
  type Enum,
  type Extensions,
  type TargetModel,
  Model,
  type ModelConverter,
  type Value,
} from './Model.js';
import { CoreModelBuilder } from './modelBuilders.js';

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
export const NumberModel = new CoreModelBuilder(PrimitiveModel, () => NaN).name('number').build();

/**
 * The model of a boolean value.
 */
export type BooleanModel = PrimitiveModel<boolean>;
export const BooleanModel = new CoreModelBuilder(PrimitiveModel, () => false).name('boolean').build();

/**
 * The symbol that represents the ArrayModel item property.
 */
export const $itemModel = Symbol('itemModel');

/**
 * The model of array data.
 */
export type ArrayModel<M extends Model = Model> = Model<
  Array<Value<M>>,
  {
    readonly [$itemModel]: M;
  }
>;
export const ArrayModel: ArrayModel = new CoreModelBuilder(Model, (): unknown[] => [])
  .name('Array')
  .define($itemModel, { value: Model })
  .build();

/**
 * The model of object data.
 */
export type ObjectModel<V, EX extends AnyObject = AnyObject> = Model<
  V,
  {
    readonly [K in keyof EX]: EX[K] extends ModelConverter ? TargetModel<EX[K], ObjectModel<V, EX>> : EX[K];
  }
>;
export const ObjectModel = new CoreModelBuilder(Model, (): AnyObject => ({})).name('Object').build();

/**
 * The model of a `Record<string, V>` data, which is a special case
 * of `ObjectModel<Record<string, V>>` that is used to represent an arbitrary
 * object with string keys, such as a Java `Map<String, Object>`.
 */
export type RecordModel<K extends string, V> = Model<Record<K, V>>;
export const RecordModel = new CoreModelBuilder(ObjectModel, (): Record<string, unknown> => ({}))
  .name('Record')
  .build();

/**
 * The symbol that represents the `EnumModel[$enumerate]` property.
 */
export const $enum = Symbol('enumerate');

/**
 * The model of enum data.
 */
export type EnumModel<T extends typeof Enum> = Model<
  T[keyof T],
  {
    readonly [$enum]: T;
  }
>;

export const EnumModel: EnumModel<typeof Enum> = new CoreModelBuilder<(typeof Enum)[keyof typeof Enum]>(Model)
  .name('Enum')
  // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
  .define($enum, { value: {} as typeof Enum })
  .defaultValueProvider((self) => Object.values(self[$enum])[0])
  .build();

/**
 * The model of a union data.
 */
export type UnionModel<MM extends Model[]> = Model<Value<MM[number]>, { readonly [$members]: MM }>;

/**
 * The model of an optional type.
 */
export type OptionalModel<M extends Model> = Model<
  M[typeof $defaultValue] | undefined,
  Extensions<M> & {
    readonly $optional: true;
  }
>;
