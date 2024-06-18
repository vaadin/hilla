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
  type Enum,
  type AnyObject,
  Model,
  type Value,
} from './model.js';

export * from './model.js';
export * from './core.js';

const m = {
  extend<SU extends AnyObject>(base: Model<SU>): ObjectModelBuilder<AnyObject, SU> {
    return new ObjectModelBuilder(base);
  },
  optional<M extends Model>(base: M): M {
    return new CoreModelBuilder(base).define($optional, { value: true }).build();
  },
  array<T, C extends AnyObject>(itemModel: Model<T, C>): ArrayModel<T, C> {
    return new CoreModelBuilder<T[]>(ArrayModel)
      .name(`Array<${itemModel[$name]}>`)
      .define($itemModel, { value: itemModel })
      .build();
  },
  object<T extends AnyObject>(
    name: string,
  ): ObjectModelBuilder<T, EmptyObject, EmptyObject, { named: true; selfRefKeys: never }> {
    return new ObjectModelBuilder(ObjectModel).name<T>(name);
  },
  enum<T extends typeof Enum>(obj: T, name: string): EnumModel<T> {
    return new CoreModelBuilder<T[keyof T]>(EnumModel).define($enum, { value: obj }).name(name).build();
  },
  union<MM extends Model[]>(...members: MM): UnionModel<MM> {
    return new CoreModelBuilder(Model, () => members[0][$defaultValue] as Value<MM[number]>)
      .name(members.map((model) => model[$name]).join(' | '))
      .define($members, { value: members })
      .build();
  },
};

export default m;
