import type { EmptyObject } from 'type-fest';
import {
  $assertSupportedModel,
  $constraints,
  type ConstrainedModel,
  type Constraint,
  type NonAttributedConstraint,
} from './Constraint.js';
import {
  Model,
  $owner,
  nothing,
  $key,
  type Target,
  type Value,
  type Extensions,
  type References,
  $name,
  type AnyObject,
  $optional,
  type Enum,
  $enum,
  $defaultValue,
  $members,
  type ModelMetadata,
} from './Model.js';
import { CoreModelBuilder, ObjectModelBuilder } from './modelBuilders.js';
import { ArrayModel, EnumModel, ObjectModel, type UnionModel, $itemModel, type OptionalModel } from './models.js';

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

/**
 * The utility object that provides methods to create and manipulate models.
 */
const m = {
  /**
   * Attaches the given model to the target.
   *
   * @param model - The model to attach.
   * @param targetProvider - The target to attach the model to. It could be a Binder
   * instance, a Signal, or another object. However, it could never be another
   * model.
   */
  attach<M extends Model>(this: void, model: M, targetProvider: () => Target<Value<M>>): M {
    const _model = new CoreModelBuilder<Value<M>, Extensions<M>, { named: false; selfRefKeys: References<M> }>(model)
      .name(`@${model[$name]}`)
      .build();
    defineProperty(_model, $owner, { get: targetProvider });

    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return _model as any;
  },

  /**
   * Creates a new model that extends the given base model.
   *
   * @param base - The base model to extend.
   */
  extend<M extends Model<AnyObject>>(
    this: void,
    base: M,
  ): ObjectModelBuilder<Value<M>, Value<M>, Extensions<M>, { named: false; selfRefKeys: References<M> }> {
    return new ObjectModelBuilder(base);
  },

  /**
   * Creates a new model that represents an optional value.
   *
   * @param base - The base model to extend.
   */
  optional<M extends Model>(this: void, base: M): OptionalModel<M> {
    return new CoreModelBuilder<Value<M> | undefined>(base)
      .name(base[$name])
      .define($optional, { value: true })
      .build() as OptionalModel<M>;
  },

  /**
   * Creates a new model that represents an array of items.
   *
   * @param itemModel - The model of the items in the array.
   */
  array<const M extends Model>(this: void, itemModel: M): ArrayModel<M> {
    return new CoreModelBuilder<Array<Value<M>>>(ArrayModel)
      .name(`Array<${itemModel[$name]}>`)
      .define($itemModel, { value: itemModel })
      .build();
  },

  /**
   * Creates a new model that represents an object.
   *
   * @param name - The name of the object.
   */
  object<T extends AnyObject>(
    this: void,
    name: string,
  ): ObjectModelBuilder<T, EmptyObject, EmptyObject, { named: true; selfRefKeys: never }> {
    return new ObjectModelBuilder(ObjectModel).name(name) as any;
  },

  /**
   * Creates a new model that represents an enum.
   *
   * @param obj - The enum object to represent.
   * @param name - The name of the model.
   */
  enum<T extends typeof Enum>(this: void, obj: T, name: string): EnumModel<T> {
    return new CoreModelBuilder<T[keyof T]>(EnumModel).define($enum, { value: obj }).name(name).build();
  },

  /**
   * Creates a new model that represents a union of the values of the given
   * models.
   *
   * @param members - The models to create the union from.
   */
  union<MM extends Model[]>(this: void, ...members: MM): UnionModel<MM> {
    return new CoreModelBuilder(Model, () => members[0][$defaultValue] as Value<MM[number]>)
      .name(members.map((model) => model[$name]).join(' | '))
      .define($members, { value: members })
      .define(Symbol.hasInstance, {
        value: (value: unknown): value is Value<MM[number]> => members.some((member) => value instanceof member),
      })
      .build();
  },

  /**
   * Iterates over the given array model yielding an item model for each item
   * the model value has.
   *
   * @param model - The array model to iterate over.
   */
  *items<V extends Model>(
    this: void,
    model: ArrayModel<V> | OptionalModel<ArrayModel<V>>,
  ): Generator<V, undefined, void> {
    const list = arrayItemModels.get(model) ?? [];
    arrayItemModels.set(model, list);
    const value = m.value(model) ?? [];

    list.length = value.length;

    for (let i = 0; i < value.length; i++) {
      if (!list[i]) {
        list[i] = new CoreModelBuilder(model[$itemModel], () => value[i])
          .name(`${model[$itemModel][$name]}[${i}]`)
          .define($key, { value: i })
          .define($owner, { value: model })
          .build();
      }

      yield list[i] as V;
    }
  },

  /**
   * Defines the metadata for the given model.
   *
   * @param model - The model to define metadata for.
   * @param metadata - The metadata to define.
   */
  meta<const M extends Model>(this: void, model: M, metadata: ModelMetadata): M {
    return new CoreModelBuilder(model).name(model[$name]).meta(metadata).build() as M;
  },

  /**
   * Applies the constraints to the given model.
   *
   * @param model - The model to apply the constraints to.
   * @param constraint - The constraint to apply.
   * @param moreConstraints - Additional constraints to apply.
   */
  constrained<const M extends Model>(
    this: void,
    model: M,
    constraint: Constraint<Value<M>>,
    ...moreConstraints: ReadonlyArray<Constraint<Value<M>>>
  ): ConstrainedModel<M> {
    const previousConstraints = m.hasConstraints(model) ? model[$constraints] : [];
    const newConstraints = [constraint, ...moreConstraints];
    for (const newConstraint of newConstraints) {
      newConstraint[$assertSupportedModel](model as Model<Value<M>>);
    }
    return new CoreModelBuilder(model)
      .name(model[$name])
      .define($constraints, { value: [...previousConstraints, ...newConstraints] })
      .build() as any;
  },

  /**
   * Replaces the default value of the given model with the given value.
   *
   * @param model - The model to replace the default value of.
   * @param defaultValue - The new default value.
   */
  withDefaultValue<const M extends Model>(this: void, model: M, defaultValue: Value<M>): M {
    return new CoreModelBuilder(model, () => defaultValue).name(model[$name]).build() as M;
  },

  /**
   * Checks if the given model has constraints.
   *
   * @param model - The model to check.
   */
  hasConstraints<const M extends Model>(this: void, model: M): model is ConstrainedModel<M> {
    return $constraints in model;
  },

  /**
   * Checks if the given value is a constraint of the given type.
   *
   * @param arg - The argument to check.
   * @param constraintType - The constraint to check against.
   */
  isConstraint<V, N extends string = string, A extends AnyObject = EmptyObject>(
    this: void,
    arg: unknown,
    constraintType: NonAttributedConstraint<V, N, A>,
  ): arg is Constraint<V, N, A> {
    if (typeof arg !== 'object') {
      return false;
    }

    let p: unknown = arg;
    do {
      if (p === constraintType) {
        return true;
      }
      p = Object.getPrototypeOf(p);
    } while (p !== undefined && p !== null);
    return false;
  },

  /**
   * Provides the value the given model represents. For attached models it will
   * be the owner value or its part, for detached models it will be the default
   * value of the model.
   *
   * @param model - The model to get the value from.
   */
  value<T>(this: void, model: Model<T>): T {
    const value = getRawValue(model);

    // If the value is `nothing`, we return the default value of the model.
    return value === nothing ? model[$defaultValue] : value;
  },
};

export default m;
