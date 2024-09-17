import type { EmptyObject } from 'type-fest';
import { CoreModelBuilder, ObjectModelBuilder } from './builders.js';
import { ArrayModel, EnumModel, ObjectModel, type UnionModel } from './core.js';
import {
  $defaultValue,
  $enum,
  $itemModel,
  $key,
  $members,
  $name,
  $optional,
  $owner,
  type AnyObject,
  type Target,
  type Enum,
  type Extensions,
  Model,
  nothing,
  type References,
  type Value,
} from './model.js';

export * from './model.js';
export * from './core.js';
export type * from './builders.js';

const { defineProperty } = Object;

const arrayItemModels = new WeakMap<ArrayModel, Model[]>();

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
   * @param target - The target to attach the model to. It could be a Binder
   * instance, a Signal, or another object. However, it could never be another
   * model.
   */
  attach<M extends Model>(model: M, target: Target<Value<M>>): M {
    const _model = new CoreModelBuilder<Value<M>, Extensions<M>, { named: false; selfRefKeys: References<M> }>(model)
      .name(`@${model[$name]}`)
      .build();
    defineProperty(_model, $owner, { value: target });
    defineProperty(target, 'model', { enumerable: true, configurable: true, value: model });

    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return _model as any;
  },

  /**
   * Creates a new model that extends the given base model.
   *
   * @param base - The base model to extend.
   */
  extend<M extends Model<AnyObject>>(
    base: M,
  ): ObjectModelBuilder<Value<M>, Value<M>, Extensions<M>, { named: false; selfRefKeys: References<M> }> {
    return new ObjectModelBuilder(base);
  },

  /**
   * Creates a new model that represents an optional value.
   *
   * @param base - The base model to extend.
   */
  optional<M extends Model>(base: M): M {
    return new CoreModelBuilder<Value<M>, Extensions<M>, { named: true; selfRefKeys: References<M> }>(base)
      .define($optional, { value: true })
      .build() as M;
  },

  /**
   * Creates a new model that represents an array of items.
   *
   * @param itemModel - The model of the items in the array.
   */
  array<M extends Model>(itemModel: M): ArrayModel<M> {
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
  enum<T extends typeof Enum>(obj: T, name: string): EnumModel<T> {
    return new CoreModelBuilder<T[keyof T]>(EnumModel).define($enum, { value: obj }).name(name).build();
  },

  /**
   * Creates a new model that represents a union of the values of the given
   * models.
   *
   * @param members - The models to create the union from.
   */
  union<MM extends Model[]>(...members: MM): UnionModel<MM> {
    return new CoreModelBuilder(Model, () => members[0][$defaultValue] as Value<MM[number]>)
      .name(members.map((model) => model[$name]).join(' | '))
      .define($members, { value: members })
      .build();
  },

  /**
   * Iterates over the given array model yielding an item model for each item
   * the model value has.
   *
   * @param model - The array model to iterate over.
   */
  *items<V extends Model>(model: ArrayModel<V>): Generator<V, undefined, void> {
    const list = arrayItemModels.get(model) ?? [];
    arrayItemModels.set(model, list);
    const value = m.value(model);

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
   * Provides the value the given model represents. For attached models it will
   * be the owner value or its part, for detached models it will be the default
   * value of the model.
   *
   * @param model - The model to get the value from.
   */
  value<T>(model: Model<T>): T {
    const value = getRawValue(model);

    // If the value is `nothing`, we return the default value of the model.
    return value === nothing ? model[$defaultValue] : value;
  },
};

export default m;
