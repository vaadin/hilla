import { CoreModelBuilder, ObjectModelBuilder } from './builders.js';
import { ArrayModel, EnumModel, ObjectModel } from './core.js';
import {
  type EmptyRecord,
  type Enum,
  Model,
  $members,
  $enum,
  $itemModel,
  $optional,
  $name,
  type ExtendedModel,
  $defaultValue,
} from './model.js';

export class m<T, U, C extends object = EmptyRecord> extends ObjectModelBuilder<T, U, C> {
  static optional<T>(base: ExtendedModel): ExtendedModel<T | undefined> {
    return CoreModelBuilder.from<T | undefined>(base).define($optional, true).build();
  }

  static array<T, C extends object>(
    itemModel: ExtendedModel<T, C>,
  ): ExtendedModel<T[], Readonly<{ [$itemModel]: ExtendedModel<T, C> }>> {
    return CoreModelBuilder.from<T[]>(ArrayModel).define($itemModel, itemModel).build();
  }

  static enum<T extends typeof Enum>(obj: T, name: string): ExtendedModel<T[keyof T], Readonly<{ [$enum]: T }>> {
    return CoreModelBuilder.from<T[keyof T]>(EnumModel).define($enum, obj).name(name).build();
  }

  static union<TT extends unknown[]>(
    ...members: ReadonlyArray<ExtendedModel<TT[number]>>
  ): ExtendedModel<TT[number], Readonly<{ [$members]: ReadonlyArray<ExtendedModel<TT[number]>> }>> {
    return CoreModelBuilder.from(Model, () => members[0][$defaultValue])
      .name(members.map((model) => model[$name]).join(' | '))
      .define($members, members)
      .build();
  }

  static object<T, U = object>(name: string): ObjectModelBuilder<T, U> {
    return ObjectModelBuilder.extend<T, U>(ObjectModel).name(name);
  }
}
