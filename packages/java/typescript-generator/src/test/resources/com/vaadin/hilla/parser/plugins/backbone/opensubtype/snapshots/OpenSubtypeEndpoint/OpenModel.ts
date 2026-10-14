import m, { StringModel } from '@vaadin/hilla-models';
import type Open from './Open.js';

const OpenModel = m
  .object<Open>('Open')
  .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type OpenModel = typeof OpenModel;

export default OpenModel;
