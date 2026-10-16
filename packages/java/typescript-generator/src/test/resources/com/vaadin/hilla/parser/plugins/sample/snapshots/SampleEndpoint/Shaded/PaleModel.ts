import { _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import ShadedModel from '../ShadedModel.js';
import type Pale from './Pale.js';

class PaleModel<T extends Pale = Pale> extends ShadedModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(PaleModel);
}

export default PaleModel;
