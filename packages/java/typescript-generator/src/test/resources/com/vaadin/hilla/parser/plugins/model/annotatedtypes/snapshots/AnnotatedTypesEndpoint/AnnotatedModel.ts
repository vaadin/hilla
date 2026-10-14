import {
  ArrayModel,
  NumberModel,
  ObjectModel,
  StringModel,
  _getPropertyModel,
  makeObjectEmptyValueCreator,
} from '@vaadin/hilla-lit-form';
import type Annotated from './Annotated.js';

class AnnotatedModel<T extends Annotated = Annotated> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(AnnotatedModel);

  get count(): NumberModel {
    return this[_getPropertyModel]('count', (parent, key) =>
      new NumberModel(parent, key, false, { meta: { javaType: 'int' } }));
  }

  get words(): ArrayModel<StringModel> {
    return this[_getPropertyModel]('words', (parent, key) =>
      new ArrayModel(parent, key, false,
        (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.lang.String' } }),
        { meta: { javaType: 'java.lang.String[]' } }));
  }
}

export default AnnotatedModel;
