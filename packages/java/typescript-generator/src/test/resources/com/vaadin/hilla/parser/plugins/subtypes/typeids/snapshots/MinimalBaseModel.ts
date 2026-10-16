import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type MinimalBase from './MinimalBase.js';

class MinimalBaseModel<T extends MinimalBase = MinimalBase> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(MinimalBaseModel);

  get id(): StringModel {
    return this[_getPropertyModel]('id', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default MinimalBaseModel;
