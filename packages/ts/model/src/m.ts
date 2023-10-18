import { BaseModelBuilder } from './BaseModelBuilder.js';
import { _name, _optional, _value, AbstractModel, type IModel, type Value } from './Model.js';
import {
  _itemModel,
  ArrayModel,
  type Enum,
  toEnum,
  type IArrayModel,
  type IEnumModel,
  type IModelOfOptional,
  EnumModel,
  _enum,
  type IUnionModel,
  _members,
} from './models.js';

export class m<T, M extends IModel<T>> extends BaseModelBuilder<T, M> {
  static optional<M extends IModel>(superModel: M): IModelOfOptional<M> {
    return m.from(superModel).define(_optional, true).build() as IModelOfOptional<M>;
  }

  static array<T, M extends IModel<T>>(itemModel: M): IArrayModel<T> {
    return m
      .from(ArrayModel, () => [] as T[])
      .name(`Array<${itemModel[_name]}>`)
      .define(_itemModel, itemModel)
      .build();
  }

  static enum<E extends typeof Enum = typeof Enum>(enumObject: E): IEnumModel<E> {
    return m
      .from(EnumModel, toEnum<E>)
      .define(_enum, enumObject)
      .build();
  }

  static union<MM extends [IModel, ...IModel[]]>(...members: MM): IUnionModel<MM> {
    return m
      .from(AbstractModel, () => members[0][_value] as Value<MM[number]>)
      .name(members.map((member: IModel) => member[_name]).join(' | '))
      .define(_members, members)
      .build();
  }
}
