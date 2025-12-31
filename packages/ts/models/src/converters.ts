import {
  $assertSupportedModel,
  $constraints,
  $name,
  $optional,
  type $targetModel,
  type Constraint,
  type CompositeOf,
  type SourceModel,
  type TargetModel,
  type Model,
  type ModelConverter,
  type ModelMetadata,
  type Value,
} from './Model.js';
import { CoreModelBuilder } from './modelBuilders.js';
import { $itemModel, ArrayModel, type OptionalModel } from './models.js';

/**
 * Function that converts the given model.
 *
 * @param model - the model to convert
 * @returns derived model
 */
export type ModelConverterFn<SM extends Model = Model, TM extends Model = Model> = (
  model: SM,
  ...extraArgs: readonly any[]
) => TM;

/**
 * Universal converter function type for models and model converter functions.
 */
export type ModelConverterCallable<
  MC extends ModelConverter,
  F extends ModelConverterFn<SourceModel<MC>, TargetModel<MC, SourceModel<MC>>>,
> = F extends (model: Model, ...extraArgs: infer E) => Model
  ? <const IMC extends ModelConverter>(modelConverter: IMC, ...extraArgs: E) => CompositeOf<MC, IMC>
  : never;

/**
 * Applies HKT signature and adds model converter callback support for the given
 * simple model converter function implementation. These additions provide
 * support for self-referencing properties in object model, i. g.,
 * `.property(m.optional(m.self))`.
 *
 * @param modelConverterFn - model function implementation
 * @returns model converter that also supports other converters as arguments,
 * for which it returns a composite model converter.
 */
function createModelConverter<
  const MC extends ModelConverter,
  const F extends ModelConverterFn<SourceModel<MC>, TargetModel<MC, SourceModel<MC>>>,
>(modelConverterFn: F): MC & F & ModelConverterCallable<MC, F> {
  return ((modelOrConverter: Model | ModelConverterFn, ...extraArgs: readonly any[]) => {
    if (typeof modelOrConverter === 'function') {
      return (model: Model) => modelConverterFn(modelOrConverter(model), ...extraArgs);
    }

    return modelConverterFn(modelOrConverter, ...extraArgs);
  }) as any;
}

/**
 * HKT signature for model converters that return models of the same data type
 * as the given model.
 */
export interface IdentityOf extends ModelConverter {
  readonly [$targetModel]: SourceModel<this>;
}
function selfImpl<const M extends Model>(model: M): M {
  return model;
}
export type ModelSelf = IdentityOf & typeof selfImpl;

/**
 * The model converter identity function that returns the given model.
 *
 * @param model - The model to return.
 */
const _self = selfImpl as ModelSelf;
export { _self as self };

/**
 * HKT signature for optional model converter, which returns optional model
 * of the given one.
 */
export interface OptionalOf extends ModelConverter {
  readonly [$targetModel]: OptionalModel<SourceModel<this>>;
}
function optionalImpl<const M extends Model>(model: M) {
  return new CoreModelBuilder(model).name(model[$name]).define($optional, { value: true }).build() as OptionalModel<M>;
}

/**
 * Creates a new model that represents an optional value.
 *
 * @param base - The base model to extend.
 */
export const optional = createModelConverter<OptionalOf, typeof optionalImpl>(optionalImpl);

/**
 * HKT signature for array model converte, which returns array model with
 * items of the given model.
 */
export interface ArrayOf extends ModelConverter {
  readonly [$targetModel]: ArrayModel<SourceModel<this>>;
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
export const array = createModelConverter<ArrayOf, typeof arrayImpl>(arrayImpl);

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
export const constrained = createModelConverter<IdentityOf, typeof constrainedImpl>(constrainedImpl);

function metaImpl<const M extends Model>(this: void, model: M, metadata: ModelMetadata): M {
  return new CoreModelBuilder(model).name(model[$name]).meta(metadata).build() as M;
}
/**
 * Defines the metadata for the given model.
 *
 * @param model - The model to define metadata for.
 * @param metadata - The metadata to define.
 */
export const meta = createModelConverter<IdentityOf, typeof metaImpl>(metaImpl);
