import m, { StringModel } from '@vaadin/hilla-models';
import type SimpleBase from './SimpleBase.js';

const SimpleBaseModel = m
  .object<SimpleBase>('SimpleBase')
  .property('id', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type SimpleBaseModel = typeof SimpleBaseModel;

export default SimpleBaseModel;
