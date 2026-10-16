import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import MinimalBaseModel from '../MinimalBaseModel.js';
import type Subtype from './Subtype.js';

class SubtypeModel<T extends Subtype = Subtype> extends MinimalBaseModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(SubtypeModel);

  get note(): StringModel {
    return this[_getPropertyModel]('note', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default SubtypeModel;
