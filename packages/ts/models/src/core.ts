import type { EmptyObject } from 'type-fest';
import { CoreModelBuilder } from './builders.js';
import { $enum, $itemModel, type $members, type AnyObject, type Enum, Model, type Value } from './model.js';

export type PrimitiveModel<V = unknown> = Model<V>;
export const PrimitiveModel = new CoreModelBuilder(Model, (): unknown => undefined).name('primitive').build();

export type StringModel = PrimitiveModel<string>;
export const StringModel = new CoreModelBuilder(PrimitiveModel, () => '').name('string').build();

export type NumberModel = PrimitiveModel<number>;
export const NumberModel = new CoreModelBuilder(PrimitiveModel, () => 0).name('number').build();

export type BooleanModel = PrimitiveModel<boolean>;
export const BooleanModel = new CoreModelBuilder(PrimitiveModel, () => false).name('boolean').build();

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

export type ObjectModel<V, EX extends AnyObject = EmptyObject, R extends string = never> = Model<V, EX, R>;

export const ObjectModel = new CoreModelBuilder(Model, (): AnyObject => ({})).name('Object').build();

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

export type UnionModel<MM extends Model[]> = Model<Value<MM[number]>, Readonly<{ [$members]: MM }>>;
