import type { EmptyObject } from 'type-fest';
import { CoreModelBuilder, ObjectModelBuilder } from './builders.js';
import { ArrayModel, EnumModel, ObjectModel, type UnionModel } from './core.js';
import {
  $defaultValue,
  $enum,
  $itemModel,
  $members,
  $name,
  $optional,
  $owner,
  type AnyObject,
  type AttachTarget,
  type Enum,
  type Extensions,
  Model,
  type References,
  type Value,
} from './model.js';

export * from './model.js';
export * from './core.js';
export * from './utils.js';

const { defineProperty } = Object;

const m = {
  attach<M extends Model>(model: M, target: AttachTarget<Value<M>>): M {
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
};

export default m;
