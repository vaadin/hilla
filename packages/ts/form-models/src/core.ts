import { CoreModelBuilder } from './builders.js';
import { $enum, $itemModel, type $members, type Enum, type AnyObject, Model, type Value } from './model.js';

export const PrimitiveModel = new CoreModelBuilder(Model, (): unknown => undefined).name('primitive').build();

export const StringModel = new CoreModelBuilder(PrimitiveModel, () => '').name('string').build();

export const NumberModel = new CoreModelBuilder(PrimitiveModel, () => 0).name('number').build();

export const BooleanModel = new CoreModelBuilder(PrimitiveModel, () => false).name('boolean').build();

export type ArrayModel<T, C extends AnyObject, R extends string = never> = Model<
  T[],
  Readonly<{
    [$itemModel]: Model<T, C, R>;
  }>
>;

export const ArrayModel = new CoreModelBuilder(Model, (): unknown[] => [])
  .name('Array')
  .define($itemModel, { value: Model })
  .build();

export type ObjectModel<T extends object, C extends object, R extends string = never> = Model<T, C, R>;

export const ObjectModel: ObjectModel<AnyObject, AnyObject> = new CoreModelBuilder(Model, (): AnyObject => ({}))
  .name('Object')
  .build();

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
