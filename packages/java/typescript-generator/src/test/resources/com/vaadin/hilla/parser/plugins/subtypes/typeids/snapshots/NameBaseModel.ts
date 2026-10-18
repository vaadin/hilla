import m, { StringModel } from '@vaadin/hilla-models';
import type NameBase from './NameBase.js';

const NameBaseModel = m
  .object<NameBase>('NameBase')
  .property('id', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type NameBaseModel = typeof NameBaseModel;

export default NameBaseModel;
