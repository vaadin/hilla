import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type FromSuperclass from './FromSuperclass.js';
import NamedParentModel from './NamedParentModel.js';

class FromSuperclassModel<T extends FromSuperclass = FromSuperclass> extends NamedParentModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(FromSuperclassModel);

  get number(): StringModel {
    return this[_getPropertyModel]('number', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default FromSuperclassModel;
