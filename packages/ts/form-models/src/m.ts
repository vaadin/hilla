import { CoreModelBuilder } from './builders.js';
import { ArrayModel, EnumModel } from './core.js';
import { type Enum, Model, type ModelConstructor } from './model.js';

export const m = {
  optional<T>(base: ModelConstructor): ModelConstructor<T | undefined> {
    return CoreModelBuilder.from<T | undefined>(base).define('optional', true).build();
  },

  array<T, C extends object>(
    itemModel: ModelConstructor<T, C>,
  ): ModelConstructor<T[], Readonly<{ itemModel: ModelConstructor<T, C> }>> {
    return CoreModelBuilder.from<T[]>(ArrayModel).define('itemModel', itemModel).build();
  },

  enum<T extends typeof Enum>(obj: T, name: string): ModelConstructor<T[keyof T], Readonly<{ enum: T }>> {
    return CoreModelBuilder.from<T[keyof T]>(EnumModel).define('enum', obj).name(name).build();
  },

  union<TT extends unknown[]>(
    ...members: ReadonlyArray<ModelConstructor<TT[number]>>
  ): ModelConstructor<TT[number], Readonly<{ members: ReadonlyArray<ModelConstructor<TT[number]>> }>> {
    return CoreModelBuilder.from(Model, () => members[0].defaultValue)
      .name(members.map((model) => model.name).join(' | '))
      .define('members', members)
      .build();
  },
};
