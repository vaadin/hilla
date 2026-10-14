import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Money from './Money.js';

class MoneyModel<T extends Money = Money> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(MoneyModel);
}

export default MoneyModel;
