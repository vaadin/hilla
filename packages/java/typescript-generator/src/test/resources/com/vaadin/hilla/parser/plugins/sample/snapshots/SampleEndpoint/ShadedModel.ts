import m, { StringModel } from '@vaadin/hilla-models';
import type Shaded from './Shaded.js';

const ShadedModel = m
  .object<Shaded>('Shaded')
  .property('shade', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type ShadedModel = typeof ShadedModel;

export default ShadedModel;
