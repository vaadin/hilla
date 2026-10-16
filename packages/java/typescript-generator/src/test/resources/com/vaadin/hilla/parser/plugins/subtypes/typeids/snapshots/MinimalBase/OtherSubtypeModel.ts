import { NumberModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import MinimalBaseModel from '../MinimalBaseModel.js';
import type OtherSubtype from './OtherSubtype.js';

class OtherSubtypeModel<T extends OtherSubtype = OtherSubtype> extends MinimalBaseModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(OtherSubtypeModel);

  get size(): NumberModel {
    return this[_getPropertyModel]('size', (parent, key) =>
      new NumberModel(parent, key, false, { meta: { javaType: 'int' } }));
  }
}

export default OtherSubtypeModel;
