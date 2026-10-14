import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Pet from './Pet.js';

class PetModel<T extends Pet = Pet> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(PetModel);

  get name(): StringModel {
    return this[_getPropertyModel]('name', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default PetModel;
