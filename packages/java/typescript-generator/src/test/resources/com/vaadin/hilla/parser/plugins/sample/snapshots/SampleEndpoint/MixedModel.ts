import m, { BooleanModel, NumberModel, ObjectModel, StringModel } from '@vaadin/hilla-models';
import KindModel from './KindModel.js';
import type Mixed from './Mixed.js';
import SampleModel from './SampleModel.js';

const MixedModel = m
  .object<Mixed>('Mixed')
  .property('count', m.meta(NumberModel, { jvmType: 'int' }))
  .property('active', m.meta(BooleanModel, { jvmType: 'boolean' }))
  .property('tags', m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.List' }))
  .property('counts', m.meta(m.optional(m.record(m.meta(m.optional(NumberModel), { jvmType: 'java.lang.Integer' }))), { jvmType: 'java.util.Map' }))
  .property('kind', m.optional(KindModel))
  .property('sample', m.optional(SampleModel))
  .property('words', m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.lang.String[]' }))
  .property('unique', m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.Set' }))
  .property('grouped', m.meta(m.optional(m.record(m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.List' }))), { jvmType: 'java.util.Map' }))
  .property('maybeTags', m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.List' }))
  .property('signal', m.optional(ObjectModel))
  .build();
type MixedModel = typeof MixedModel;

export default MixedModel;
