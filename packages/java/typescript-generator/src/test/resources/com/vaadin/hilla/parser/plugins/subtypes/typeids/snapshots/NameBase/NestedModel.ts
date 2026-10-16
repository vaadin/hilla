import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import NameBaseModel from '../NameBaseModel.js';
import type Nested from './Nested.js';

class NestedModel<T extends Nested = Nested> extends NameBaseModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(NestedModel);

  get note(): StringModel {
    return this[_getPropertyModel]('note', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default NestedModel;
