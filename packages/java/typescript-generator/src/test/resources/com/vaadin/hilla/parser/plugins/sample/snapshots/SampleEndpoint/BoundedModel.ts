import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Bounded from './Bounded.js';
import SampleModel from './SampleModel.js';

class BoundedModel<T extends Bounded = Bounded> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(BoundedModel);

  get held(): SampleModel {
    return this[_getPropertyModel]('held', (parent, key) =>
      new SampleModel(parent, key, true));
  }
}

export default BoundedModel;
