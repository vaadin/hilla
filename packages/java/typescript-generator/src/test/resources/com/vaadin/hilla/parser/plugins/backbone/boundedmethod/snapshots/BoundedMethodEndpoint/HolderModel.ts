import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Holder from './Holder.js';

class HolderModel<T extends Holder = Holder> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(HolderModel);

  get value(): ObjectModel {
    return this[_getPropertyModel]('value', (parent, key) =>
      new ObjectModel(parent, key, true));
  }

  get maybe(): ObjectModel {
    return this[_getPropertyModel]('maybe', (parent, key) =>
      new ObjectModel(parent, key, true));
  }
}

export default HolderModel;
