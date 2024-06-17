import { CoreModelBuilder, ObjectModelBuilder } from './builders.js';
import { ArrayModel, EnumModel, type ModelLike, ObjectModel, type UnionModel } from './core.js';
import {
  $defaultValue,
  $enum,
  $itemModel,
  $members,
  $name,
  $optional,
  type EmptyRecord,
  type Enum,
  Model,
  type ModelValue,
} from './model.js';

export * from './model.js';
export * from './core.js';

const m = {
  extend<SU extends object>(base: Model<SU>): ObjectModelBuilder<object, SU> {
    return new ObjectModelBuilder(base);
  },
  optional<T>(base: Model): Model<T | undefined> {
    return new CoreModelBuilder<T | undefined>(base).define($optional, true).build();
  },
  array<T>(itemModel: Model<T>): ArrayModel<T> {
    return new CoreModelBuilder<T[]>(ArrayModel)
      .name(`Array<${itemModel[$name]}>`)
      .define($itemModel, itemModel as ModelLike<T>)
      .build();
  },
  object<T extends object>(name: string): ObjectModelBuilder<T, object, EmptyRecord, true> {
    return m.extend(ObjectModel).name<T>(name);
  },
  enum<T extends typeof Enum>(obj: T, name: string): EnumModel<T> {
    return new CoreModelBuilder<T[keyof T]>(EnumModel).define($enum, obj).name(name).build();
  },
  union<TT extends unknown[]>(...members: ReadonlyArray<Model<TT[number]>>): UnionModel<TT> {
    return new CoreModelBuilder(Model, () => members[0][$defaultValue] as ModelValue<TT[number]>)
      .name(members.map((model) => model.constructor.name).join(' | '))
      .define($members, members as ReadonlyArray<ModelLike<TT[number]>>)
      .build();
  },
};

export default m;
