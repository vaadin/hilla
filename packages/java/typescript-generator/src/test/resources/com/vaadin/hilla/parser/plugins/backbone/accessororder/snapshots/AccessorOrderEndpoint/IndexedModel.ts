import m, { StringModel } from '@vaadin/hilla-models';
import type Indexed from './Indexed.js';

const IndexedModel = m
  .object<Indexed>('Indexed')
  .property('zeta', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('middle', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('alpha', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type IndexedModel = typeof IndexedModel;

export default IndexedModel;
