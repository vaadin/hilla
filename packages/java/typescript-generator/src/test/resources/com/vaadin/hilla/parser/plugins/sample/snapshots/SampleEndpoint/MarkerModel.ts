import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Marker from './Marker.js';

class MarkerModel<T extends Marker = Marker> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(MarkerModel);
}

export default MarkerModel;
