import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type Detailed from './Detailed.js';
import SampleModel from './SampleModel.js';

class DetailedModel<T extends Detailed = Detailed> extends SampleModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(DetailedModel);

  get note(): StringModel {
    return this[_getPropertyModel]('note', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }

  get basis(): SampleModel {
    return this[_getPropertyModel]('basis', (parent, key) =>
      new SampleModel(parent, key, true));
  }
}

export default DetailedModel;
