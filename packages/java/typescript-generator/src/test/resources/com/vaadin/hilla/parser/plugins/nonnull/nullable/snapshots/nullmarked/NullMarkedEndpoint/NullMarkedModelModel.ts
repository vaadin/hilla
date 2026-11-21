import m, { StringModel } from '@vaadin/hilla-models';
import type NullMarkedModel from './NullMarkedModel.js';

const NullMarkedModelModel = m
  .object<NullMarkedModel>('NullMarkedModel')
  .property('required', m.meta(StringModel, { jvmType: 'java.lang.String' }))
  .property('nullable', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('nullableGetter', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('nullableGetterItems', m.meta(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' })), { jvmType: 'java.util.List' }))
  .property('nullableSetter', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('nullableSetterItems', m.meta(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' })), { jvmType: 'java.util.List' }))
  .build();
type NullMarkedModelModel = typeof NullMarkedModelModel;

export default NullMarkedModelModel;
