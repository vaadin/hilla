import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Wrapper from './Wrapper.js';

class WrapperModel<T extends Wrapper = Wrapper> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(WrapperModel);

  get value(): ObjectModel {
    return this[_getPropertyModel]('value', (parent, key) =>
      new ObjectModel(parent, key, true));
  }
}

export default WrapperModel;
