import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Inner from './Inner.js';
import WrappedModel from './WrappedModel.js';

class InnerModel<T extends Inner = Inner> extends WrappedModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(InnerModel);

  get value(): StringModel {
    return this[_getPropertyModel]('value', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default InnerModel;
