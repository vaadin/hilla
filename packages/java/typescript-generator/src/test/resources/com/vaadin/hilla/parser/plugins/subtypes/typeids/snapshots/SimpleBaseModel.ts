import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type SimpleBase from './SimpleBase.js';

class SimpleBaseModel<T extends SimpleBase = SimpleBase> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(SimpleBaseModel);

  get id(): StringModel {
    return this[_getPropertyModel]('id', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default SimpleBaseModel;
