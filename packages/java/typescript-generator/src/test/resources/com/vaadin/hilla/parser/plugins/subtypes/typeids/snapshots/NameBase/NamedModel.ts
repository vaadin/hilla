import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import NameBaseModel from '../NameBaseModel.js';
import type Named from './Named.js';

class NamedModel<T extends Named = Named> extends NameBaseModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(NamedModel);

  get name(): StringModel {
    return this[_getPropertyModel]('name', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default NamedModel;
