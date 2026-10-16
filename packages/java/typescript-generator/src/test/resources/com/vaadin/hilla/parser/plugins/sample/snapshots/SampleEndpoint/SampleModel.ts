import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Sample from './Sample.js';
import SampleModel_1 from './SampleModel.js';

class SampleModel<T extends Sample = Sample> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(SampleModel);

  get name(): StringModel {
    return this[_getPropertyModel]('name', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }

  get parent(): SampleModel_1 {
    return this[_getPropertyModel]('parent', (parent, key) =>
      new SampleModel_1(parent, key, true));
  }

  get label(): StringModel {
    return this[_getPropertyModel]('label', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default SampleModel;
