import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type ExposedInterfaceEntity from './ExposedInterfaceEntity.js';

class ExposedInterfaceEntityModel<T extends ExposedInterfaceEntity = ExposedInterfaceEntity> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(ExposedInterfaceEntityModel);
}

export default ExposedInterfaceEntityModel;
