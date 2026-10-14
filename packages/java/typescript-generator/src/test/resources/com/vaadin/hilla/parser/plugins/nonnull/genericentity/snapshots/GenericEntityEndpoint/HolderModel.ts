import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import BoxModel from './BoxModel.js';
import type Holder from './Holder.js';

class HolderModel<T extends Holder = Holder> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(HolderModel);

  get boxOfMaybe(): BoxModel {
    return this[_getPropertyModel]('boxOfMaybe', (parent, key) =>
      new BoxModel(parent, key, false));
  }

  get maybeBoxOf(): BoxModel {
    return this[_getPropertyModel]('maybeBoxOf', (parent, key) =>
      new BoxModel(parent, key, true));
  }
}

export default HolderModel;
