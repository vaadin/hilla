import m, { StringModel } from '@vaadin/hilla-models';
import type PartlyOrdered from './PartlyOrdered.js';

const PartlyOrderedModel = m
  .object<PartlyOrdered>('PartlyOrdered')
  .property('zeta', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('field', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('omega', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('alpha', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('middle', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type PartlyOrderedModel = typeof PartlyOrderedModel;

export default PartlyOrderedModel;
