import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type NameBase from './NameBase.js';

class NameBaseModel<T extends NameBase = NameBase> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(NameBaseModel);

  get id(): StringModel {
    return this[_getPropertyModel]('id', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default NameBaseModel;
