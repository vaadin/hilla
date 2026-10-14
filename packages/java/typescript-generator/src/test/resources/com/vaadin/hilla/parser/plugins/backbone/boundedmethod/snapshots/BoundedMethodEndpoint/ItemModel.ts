import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Item from './Item.js';

class ItemModel<T extends Item = Item> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(ItemModel);

  get name(): StringModel {
    return this[_getPropertyModel]('name', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default ItemModel;
