import { NumberModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import FigureModel from '../FigureModel.js';
import type Round from './Round.js';

class RoundModel<T extends Round = Round> extends FigureModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(RoundModel);

  get radius(): NumberModel {
    return this[_getPropertyModel]('radius', (parent, key) =>
      new NumberModel(parent, key, false, { meta: { javaType: 'double' } }));
  }
}

export default RoundModel;
