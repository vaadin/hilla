import { _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import FigureModel from '../FigureModel.js';
import type Blank from './Blank.js';

class BlankModel<T extends Blank = Blank> extends FigureModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(BlankModel);
}

export default BlankModel;
