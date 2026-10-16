import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Shaded from './Shaded.js';

class ShadedModel<T extends Shaded = Shaded> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(ShadedModel);

  get shade(): StringModel {
    return this[_getPropertyModel]('shade', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default ShadedModel;
