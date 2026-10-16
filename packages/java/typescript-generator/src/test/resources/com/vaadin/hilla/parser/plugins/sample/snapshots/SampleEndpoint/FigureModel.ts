import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Figure from './Figure.js';

class FigureModel<T extends Figure = Figure> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(FigureModel);

  get label(): StringModel {
    return this[_getPropertyModel]('label', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default FigureModel;
