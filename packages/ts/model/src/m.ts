import { CoreModelBuilder } from './CoreModelBuilder.js';
import {
  _itemModel,
  ArrayModel,
  type Enum,
  toEnum,
  type IArrayModel,
  type IEnumModel,
  type IOptionalModel,
  EnumModel,
  _enum,
  type IUnionModel,
  _members,
  type OptionalModel,
} from './coreModels.js';
import { _name, _optional, _value, AbstractModel, type IModel, type Value } from './Model.js';
import { ObjectModelBuilder } from './ObjectModelBuilder.js';

export class m<T, M extends IModel<T>> extends ObjectModelBuilder<T, M> {
  static optional<M extends IModel>(superModel: M): OptionalModel<M> {
    return CoreModelBuilder.from(superModel).define(_optional, true).build() as OptionalModel<M>;
  }

  static array<M extends IModel>(itemModel: M): IArrayModel<M> {
    return CoreModelBuilder.from(ArrayModel, () => [] as Array<Value<M>>)
      .name(`Array<${itemModel[_name]}>`)
      .define(_itemModel, itemModel)
      .build();
  }

  static enum<E extends typeof Enum = typeof Enum>(enumObject: E, name: string): IEnumModel<E> {
    return CoreModelBuilder.from(EnumModel, toEnum<E>)
      .define(_enum, enumObject)
      .name(`enum ${name}`)
      .build();
  }

  static union<MM extends [IModel, ...IModel[]]>(...members: MM): IUnionModel<MM> {
    return CoreModelBuilder.from(AbstractModel, () => members[0][_value] as Value<MM[number]>)
      .name(members.map((member: IModel) => member[_name]).join(' | '))
      .define(_members, members)
      .build();
  }
}
