import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import NameBaseModel from '../NameBaseModel.js';
import type Listed from './Listed.js';

class ListedModel<T extends Listed = Listed> extends NameBaseModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(ListedModel);

  get tag(): StringModel {
    return this[_getPropertyModel]('tag', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default ListedModel;
