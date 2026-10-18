import m, { StringModel } from '@vaadin/hilla-models';
import type Named from './Named.js';

const NamedModel = m
  .object<Named>('Named')
  .property('first-name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('2nd', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('delete', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type NamedModel = typeof NamedModel;

export default NamedModel;
