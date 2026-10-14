import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Open from './Open.js';

class OpenModel<T extends Open = Open> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(OpenModel);

  get name(): StringModel {
    return this[_getPropertyModel]('name', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default OpenModel;
