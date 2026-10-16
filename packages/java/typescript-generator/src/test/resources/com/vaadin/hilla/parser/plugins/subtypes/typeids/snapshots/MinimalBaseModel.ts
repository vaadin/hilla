import m, { StringModel } from '@vaadin/hilla-models';
import type MinimalBase from './MinimalBase.js';

const MinimalBaseModel = m
  .object<MinimalBase>('MinimalBase')
  .property('id', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type MinimalBaseModel = typeof MinimalBaseModel;

export default MinimalBaseModel;
