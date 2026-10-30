import m, { StringModel } from '@vaadin/hilla-models';
import type InheritedBase from './InheritedBase.js';

const InheritedBaseModel = m
  .object<InheritedBase>('InheritedBase')
  .property('flavour', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('label', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type InheritedBaseModel = typeof InheritedBaseModel;

export default InheritedBaseModel;
