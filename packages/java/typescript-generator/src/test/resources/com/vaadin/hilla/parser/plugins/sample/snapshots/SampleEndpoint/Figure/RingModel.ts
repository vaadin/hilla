import { NumberModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Ring from './Ring.js';
import RoundModel from './RoundModel.js';

class RingModel<T extends Ring = Ring> extends RoundModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(RingModel);

  get hole(): NumberModel {
    return this[_getPropertyModel]('hole', (parent, key) =>
      new NumberModel(parent, key, false, { meta: { javaType: 'double' } }));
  }
}

export default RingModel;
