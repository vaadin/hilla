import type { EmptyObject } from 'type-fest';
import {
  $assertSupportedModel,
  type Constraint,
  type NonAttributedConstraint,
  Model,
  $owner,
  nothing,
  $key,
  type Target,
  type Value,
  type Extensions,
  $name,
  type AnyObject,
  $optional,
  type Enum,
  $defaultValue,
  $constraints,
  $members,
  type ModelMetadata,
  type ModelConverter,
  type $to,
  type MCFrom,
  type MCTo,
  type MCCompose,
} from './Model.js';
import { CoreModelBuilder, ObjectModelBuilder } from './modelBuilders.js';
import {
  ArrayModel,
  EnumModel,
  ObjectModel,
  type UnionModel,
  $itemModel,
  type OptionalModel,
  $enum,
} from './models.js';

const { defineProperty } = Object;

const arrayItemModels = new WeakMap<ArrayModel | OptionalModel<ArrayModel>, Model[]>();

function getRawValue<T>(model: Model<T>): T | typeof nothing {
  if (model[$owner] instanceof Model) {
    // If the current model is a property of another model, the owner is
    // definitely an object. So we just return the part of the value of
    // the owner.
    const parentValue = getRawValue(model[$owner] as Model<Record<keyof any, T>>);
    return parentValue === nothing ? nothing : parentValue[model[$key]];
  }

  // Otherwise, the owner is a Target, so we can return the full value.
  return (model[$owner] as Target<T>).value;
}

export type ModelConverterFn<FM extends Model = Model, TM extends Model = Model> = (
  model: FM,
  ...extraArgs: any[]
) => TM;

export type ModelConverterCallable<
  MC extends ModelConverter,
  F extends ModelConverterFn<MCFrom<MC>, MCTo<MC, MCFrom<MC>>>,
> = F extends (model: Model, ...extraArgs: infer E) => Model
  ? <const IMC extends ModelConverter>(modelConverter: IMC, ...extraArgs: E) => MCCompose<MC, IMC>
  : never;

function createModelConverter<
  const MC extends ModelConverter,
  const F extends ModelConverterFn<MCFrom<MC>, MCTo<MC, MCFrom<MC>>>,
>(modelConverterFn: F): MC & F & ModelConverterCallable<MC, F> {
  return ((modelOrConverter: Model | ModelConverterFn, ...extraArgs: any[]) => {
    if (typeof modelOrConverter === 'function') {
      return (model: Model) => modelConverterFn(modelOrConverter(model), ...extraArgs);
    }

    return modelConverterFn(modelOrConverter, ...extraArgs);
  }) as any;
}

export interface IdentityModelConverter extends ModelConverter {
  readonly [$to]: MCFrom<this>;
}
function selfImpl<const M extends Model>(model: M): M {
  return model;
}
const _self = createModelConverter<IdentityModelConverter, typeof selfImpl>(selfImpl);
export { _self as self };

export interface OptionalModelConverter extends ModelConverter {
  readonly [$to]: OptionalModel<MCFrom<this>>;
}
function optionalImpl<const M extends Model>(model: M) {
  return new CoreModelBuilder(model).name(model[$name]).define($optional, { value: true }).build() as OptionalModel<M>;
}
/**
 * Creates a new model that represents an optional value.
 *
 * @param base - The base model to extend.
 */
export const optional = createModelConverter<OptionalModelConverter, typeof optionalImpl>(optionalImpl);

export interface ArrayModelConverter extends ModelConverter {
  readonly [$to]: ArrayModel<MCFrom<this>>;
}
function arrayImpl<const M extends Model>(model: M) {
  return new CoreModelBuilder<Array<Value<M>>>(ArrayModel, (): Array<Value<M>> => [])
    .name(`Array<${model[$name]}>`)
    .define($itemModel, { value: model })
    .build() as ArrayModel<M>;
}
/**
 * Creates a new model that represents an array of items.
 *
 * @param itemModel - The model of the items in the array.
 */
export const array = createModelConverter<ArrayModelConverter, typeof arrayImpl>(arrayImpl);

/**
 * Attaches the given model to the target.
 *
 * @param model - The model to attach.
 * @param targetProvider - The target to attach the model to. It could be a Binder
 * instance, a Signal, or another object. However, it could never be another
 * model.
 */
export function attach<M extends Model>(this: void, model: M, targetProvider: () => Target<Value<M>>): M {
  const _model = new CoreModelBuilder<Value<M>, Extensions<M>, { named: false }>(model)
    .name(`@${model[$name]}`)
    .build();
  defineProperty(_model, $owner, { get: targetProvider });

  // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
  return _model as any;
}

/**
 * Creates a new model that extends the given base model.
 *
 * @param base - The base model to extend.
 */
export function extend<M extends Model<AnyObject>>(
  this: void,
  base: M,
): ObjectModelBuilder<Value<M>, Value<M>, Extensions<M>> {
  return new ObjectModelBuilder(base);
}

/**
 * Creates a new model that represents an object.
 *
 * @param name - The name of the object.
 */
export function object<T extends AnyObject>(
  this: void,
  name: string,
): ObjectModelBuilder<T, AnyObject, AnyObject, AnyObject, { named: true }> {
  return new ObjectModelBuilder(ObjectModel).name(name) as any;
}

/**
 * Creates a new model that represents an enum.
 *
 * @param obj - The enum object to represent.
 * @param name - The name of the model.
 */
function _enum<T extends typeof Enum>(this: void, obj: T, name: string): EnumModel<T> {
  return new CoreModelBuilder<T[keyof T]>(EnumModel).define($enum, { value: obj }).name(name).build();
}
export { _enum as enum };

/**
 * Creates a new model that represents a union of the values of the given
 * models.
 *
 * @param members - The models to create the union from.
 */
export function union<MM extends Model[]>(this: void, ...members: MM): UnionModel<MM> {
  return new CoreModelBuilder(Model, () => members[0][$defaultValue] as Value<MM[number]>)
    .name(members.map((model) => model[$name]).join(' | '))
    .define($members, { value: members })
    .define(Symbol.hasInstance, {
      value: (v: unknown): v is Value<MM[number]> => members.some((member) => v instanceof member),
    })
    .build();
}

/**
 * Provides the value the given model represents. For attached models it will
 * be the owner value or its part, for detached models it will be the default
 * value of the model.
 *
 * @param model - The model to get the value from.
 */
export function getValue<T>(this: void, model: Model<T>): T {
  const v = getRawValue(model);

  // If the value is `nothing`, we return the default value of the model.
  return v === nothing ? model[$defaultValue] : v;
}

/**
 * Iterates over the given array model yielding an item model for each item
 * the model value has.
 *
 * @param model - The array model to iterate over.
 */
export function* items<V extends Model>(
  this: void,
  model: ArrayModel<V> | OptionalModel<ArrayModel<V>>,
): Generator<V, undefined, void> {
  const list = arrayItemModels.get(model) ?? [];
  arrayItemModels.set(model, list);
  const v = getValue(model) ?? [];

  list.length = v.length;

  for (let i = 0; i < v.length; i++) {
    if (!list[i]) {
      list[i] = new CoreModelBuilder(model[$itemModel], () => v[i])
        .name(`${model[$itemModel][$name]}[${i}]`)
        .define($key, { value: i })
        .define($owner, { value: model })
        .build();
    }

    yield list[i] as V;
  }
}

function metaImpl<const M extends Model>(this: void, model: M, metadata: ModelMetadata): M {
  return new CoreModelBuilder(model).name(model[$name]).meta(metadata).build() as M;
}
/**
 * Defines the metadata for the given model.
 *
 * @param model - The model to define metadata for.
 * @param metadata - The metadata to define.
 */
export const meta = createModelConverter<IdentityModelConverter, typeof metaImpl>(metaImpl);

function constrainedImpl<const M extends Model>(
  this: void,
  model: M,
  constraint: Constraint<Value<M>>,
  ...moreConstraints: ReadonlyArray<Constraint<Value<M>>>
): M {
  const previousConstraints = model[$constraints];
  const newConstraints = [constraint, ...moreConstraints];
  for (const newConstraint of newConstraints) {
    newConstraint[$assertSupportedModel](model as Model<Value<M>>);
  }
  return new CoreModelBuilder(model)
    .name(model[$name])
    .define($constraints, { value: [...previousConstraints, ...newConstraints] })
    .build() as any;
}
/**
 * Applies the constraints to the given model.
 *
 * @param model - The model to apply the constraints to.
 * @param constraint - The constraint to apply.
 * @param moreConstraints - Additional constraints to apply.
 */
export const constrained = createModelConverter<IdentityModelConverter, typeof constrainedImpl>(constrainedImpl);

/**
 * Replaces the default value of the given model with the given value.
 *
 * @param model - The model to replace the default value of.
 * @param defaultValue - The new default value.
 */
export function withDefaultValue<const M extends Model>(this: void, model: M, defaultValue: Value<M>): M {
  return new CoreModelBuilder(model, () => defaultValue).name(model[$name]).build() as M;
}

/**
 * Checks if the given value is a constraint of the given type.
 *
 * @param arg - The argument to check.
 * @param constraintType - The constraint to check against.
 */
export function isConstraint<V, N extends string = string, A extends AnyObject = EmptyObject>(
  this: void,
  arg: unknown,
  constraintType: NonAttributedConstraint<V, N, A>,
): arg is Constraint<V, N, A> {
  let p: unknown = arg;
  do {
    if (p === constraintType) {
      return true;
    }
    if (typeof p !== 'object') {
      return false;
    }
    p = Object.getPrototypeOf(p);
  } while (p !== undefined && p !== null);
  return false;
}
