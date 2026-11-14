import m, { StringModel } from '@vaadin/hilla-models';
import type Ordered from './Ordered.js';

const OrderedModel = m
  .object<Ordered>('Ordered')
  .property('field', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('alpha', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('middle', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('zeta', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type OrderedModel = typeof OrderedModel;

export default OrderedModel;
