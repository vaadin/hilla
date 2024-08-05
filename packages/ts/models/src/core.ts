import type { EmptyObject } from 'type-fest';
import { CoreModelBuilder } from './builders.js';
import {
  type $defaultValue,
  $enum,
  $itemModel,
  type $members,
  type $optional,
  type AnyObject,
  type Enum,
  Model,
  type Value,
} from './model.js';

/* eslint-disable tsdoc/syntax */

/**
 * The symbol that represents the {@link PrimitiveModel[$parse]} property.
 */
export const $parse = Symbol('parse');

/* eslint-enable tsdoc/syntax */

/**
 * The model of a primitive value, like `string`, `number` or `boolean`.
 */
export type PrimitiveModel<V = unknown> = Model<V, Readonly<{ [$parse](value: unknown): V }>>;
export const PrimitiveModel = CoreModelBuilder.create(Model, (): unknown => undefined)
  .name('primitive')
  .define($parse, {
    value: (value: unknown) => String(value),
  })
  .build();

/**
 * The model of a string value.
 */
export type StringModel = PrimitiveModel<string>;
export const StringModel = CoreModelBuilder.create(PrimitiveModel, () => '')
  .name('string')
  .build();

/**
 * The model of a number value.
 */
export type NumberModel = PrimitiveModel<number>;
export const NumberModel = CoreModelBuilder.create(PrimitiveModel, () => 0)
  .name('number')
  .define($parse, {
    value: (value: unknown) => Number(String(value)),
  })
  .build();

/**
 * The model of a boolean value.
 */
export type BooleanModel = PrimitiveModel<boolean>;
export const BooleanModel = CoreModelBuilder.create(PrimitiveModel, () => false)
  .name('boolean')
  .define($parse, {
    value: (value: unknown) => String(value) !== '',
  })
  .build();

/**
 * The model of an array data.
 */
export type ArrayModel<V = unknown, EX extends AnyObject = EmptyObject, R extends keyof any = never> = Model<
  V[],
  Readonly<{
    [$itemModel]: Model<V, EX, R>;
  }>
>;

export const ArrayModel = CoreModelBuilder.create(Model, (): unknown[] => [])
  .name('Array')
  .define($itemModel, { value: Model })
  .build();

/**
 * The model of an object data.
 */
export type ObjectModel<
  V extends AnyObject = Record<keyof any, unknown>,
  EX extends AnyObject = Readonly<{
    [P in keyof V]: Model<V[P]>;
  }>,
  R extends keyof any = never,
> = Model<V, EX, R>;

export const ObjectModel = CoreModelBuilder.create(Model, (): AnyObject => ({}))
  .name('Object')
  .build();

/**
 * The model of an enum data.
 */
export type EnumModel<T extends typeof Enum> = Model<
  T[keyof T],
  Readonly<{
    [$enum]: T;
  }>
>;

export const EnumModel = CoreModelBuilder.create(Model)
  .name<number | string>('Enum')
  // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
  .define($enum, { value: {} as typeof Enum })
  .defaultValueProvider((self) => Object.values(self[$enum])[0])
  .build();

export type OptionalModel<T = unknown, EX extends AnyObject = EmptyObject, R extends string = never> =
  Extract<T, undefined> extends never
    ? Model<T, EX, R>
    : Model<T, EX & Readonly<{ [$defaultValue]: undefined; [$optional]: true }>, R>;

/**
 * The model of a union data.
 */
export type UnionModel<MM extends Model[]> = Model<Value<MM[number]>, Readonly<{ [$members]: MM }>>;
