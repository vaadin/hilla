import { BooleanModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Cat from './Cat.js';
import PetModel from './PetModel.js';

class CatModel<T extends Cat = Cat> extends PetModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(CatModel);

  get indoor(): BooleanModel {
    return this[_getPropertyModel]('indoor', (parent, key) =>
      new BooleanModel(parent, key, false, { meta: { javaType: 'boolean' } }));
  }
}

export default CatModel;
