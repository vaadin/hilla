import m, { StringModel } from '@vaadin/hilla-models';
import type Sample from './Sample.js';

const SampleModel = m
  .object<Sample>('Sample')
  .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('parent', m.optional(m.self))
  .property('label', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type SampleModel = typeof SampleModel;

export default SampleModel;
