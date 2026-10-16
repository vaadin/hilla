import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type FromInterface from './FromInterface.js';
import NamedParentModel from './NamedParentModel.js';

class FromInterfaceModel<T extends FromInterface = FromInterface> extends NamedParentModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(FromInterfaceModel);

  get address(): StringModel {
    return this[_getPropertyModel]('address', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default FromInterfaceModel;
