import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Box from './Box.js';

class BoxModel<T extends Box = Box> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(BoxModel);

  get content(): ObjectModel {
    return this[_getPropertyModel]('content', (parent, key) =>
      new ObjectModel(parent, key, true));
  }
}

export default BoxModel;
