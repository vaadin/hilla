import { CoreModelBuilder } from './builders.js';
import { $enum, $itemModel, type $members, type Enum, Model } from './model.js';

export const PrimitiveModel = new CoreModelBuilder(Model, (): unknown => undefined).name('primitive').build();

export const StringModel = new CoreModelBuilder(PrimitiveModel, () => '').name('string').build();

export const NumberModel = new CoreModelBuilder(PrimitiveModel, () => 0).name('number').build();

export const BooleanModel = new CoreModelBuilder(PrimitiveModel, () => false).name('boolean').build();

export type ModelLike<T = unknown> = T extends boolean | number | string
  ? Model<T>
  : T extends typeof Enum
    ? EnumModel<T>
    : T extends unknown[]
      ? ArrayModel<T[number]>
      : T extends object
        ? ObjectModel<T>
        : Model<T>;

export interface ArrayModel<T> extends Model<T[]> {
  readonly [$itemModel]: ModelLike<T>;
}

export const ArrayModel = new CoreModelBuilder(Model, (): unknown[] => [])
  .name('Array')
  .define($itemModel, Model)
  .build();

export type ObjectModel<T extends object = object> = Model<T> &
  Readonly<{
    [K in keyof T]: ModelLike<T[K]>;
  }>;

export const ObjectModel = new CoreModelBuilder(Model, (): object => ({})).name('Object').build();

export interface EnumModel<T extends typeof Enum> extends Model<T[keyof T]> {
  readonly [$enum]: T;
}

export const EnumModel = new CoreModelBuilder<typeof Enum>(
  Model,
  (): (typeof Enum)[keyof typeof Enum] => Object.values(EnumModel[$enum])[0],
)
  .name('Enum')
  .define($enum, {} as typeof Enum)
  .build();

export interface UnionModel<TT extends unknown[]> extends Model<TT[number]> {
  readonly [$members]: ReadonlyArray<ModelLike<TT[number]>>;
}
