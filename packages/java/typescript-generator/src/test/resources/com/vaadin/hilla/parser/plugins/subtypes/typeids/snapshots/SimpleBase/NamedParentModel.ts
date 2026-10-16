import { NumberModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import SimpleBaseModel from '../SimpleBaseModel.js';
import type NamedParent from './NamedParent.js';

class NamedParentModel<T extends NamedParent = NamedParent> extends SimpleBaseModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(NamedParentModel);

  get size(): NumberModel {
    return this[_getPropertyModel]('size', (parent, key) =>
      new NumberModel(parent, key, false, { meta: { javaType: 'int' } }));
  }
}

export default NamedParentModel;
