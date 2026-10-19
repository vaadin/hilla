import m, { StringModel } from '@vaadin/hilla-models';
import type DeclaredBase from './DeclaredBase.js';

const DeclaredBaseModel = m
  .object<DeclaredBase>('DeclaredBase')
  .property('label', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type DeclaredBaseModel = typeof DeclaredBaseModel;

export default DeclaredBaseModel;
