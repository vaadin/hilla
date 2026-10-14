import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Count from './Count.js';

class CountModel<T extends Count = Count> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(CountModel);
}

export default CountModel;
