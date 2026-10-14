import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Wrapped from './Wrapped.js';

class WrappedModel<T extends Wrapped = Wrapped> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(WrappedModel);
}

export default WrappedModel;
