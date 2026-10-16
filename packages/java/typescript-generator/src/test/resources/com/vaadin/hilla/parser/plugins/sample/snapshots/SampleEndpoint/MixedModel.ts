import {
  ArrayModel,
  BooleanModel,
  NumberModel,
  ObjectModel,
  StringModel,
  _getPropertyModel,
  makeObjectEmptyValueCreator,
} from '@vaadin/hilla-lit-form';
import KindModel from './KindModel.js';
import type Mixed from './Mixed.js';
import SampleModel from './SampleModel.js';

class MixedModel<T extends Mixed = Mixed> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(MixedModel);

  get count(): NumberModel {
    return this[_getPropertyModel]('count', (parent, key) =>
      new NumberModel(parent, key, false, { meta: { javaType: 'int' } }));
  }

  get active(): BooleanModel {
    return this[_getPropertyModel]('active', (parent, key) =>
      new BooleanModel(parent, key, false, { meta: { javaType: 'boolean' } }));
  }

  get tags(): ArrayModel<StringModel> {
    return this[_getPropertyModel]('tags', (parent, key) =>
      new ArrayModel(parent, key, true,
        (parent, key) => new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }),
        { meta: { javaType: 'java.util.List' } }));
  }

  get counts(): ObjectModel<Record<string, number | undefined>> {
    return this[_getPropertyModel]('counts', (parent, key) =>
      new ObjectModel(parent, key, true, { meta: { javaType: 'java.util.Map' } }));
  }

  get kind(): KindModel {
    return this[_getPropertyModel]('kind', (parent, key) =>
      new KindModel(parent, key, true));
  }

  get sample(): SampleModel {
    return this[_getPropertyModel]('sample', (parent, key) =>
      new SampleModel(parent, key, true));
  }

  get words(): ArrayModel<StringModel> {
    return this[_getPropertyModel]('words', (parent, key) =>
      new ArrayModel(parent, key, true,
        (parent, key) => new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }),
        { meta: { javaType: 'java.lang.String[]' } }));
  }

  get unique(): ArrayModel<StringModel> {
    return this[_getPropertyModel]('unique', (parent, key) =>
      new ArrayModel(parent, key, true,
        (parent, key) => new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }),
        { meta: { javaType: 'java.util.Set' } }));
  }

  get grouped(): ObjectModel<Record<string, ReadonlyArray<string | undefined> | undefined>> {
    return this[_getPropertyModel]('grouped', (parent, key) =>
      new ObjectModel(parent, key, true, { meta: { javaType: 'java.util.Map' } }));
  }

  get maybeTags(): ArrayModel<StringModel> {
    return this[_getPropertyModel]('maybeTags', (parent, key) =>
      new ArrayModel(parent, key, true,
        (parent, key) => new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }),
        { meta: { javaType: 'java.util.List' } }));
  }

  get signal(): ObjectModel {
    return this[_getPropertyModel]('signal', (parent, key) =>
      new ObjectModel(parent, key, true));
  }
}

export default MixedModel;
