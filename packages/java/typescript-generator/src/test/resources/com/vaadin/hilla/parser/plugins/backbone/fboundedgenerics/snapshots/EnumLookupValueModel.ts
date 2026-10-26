import m, { Model, StringModel } from '@vaadin/hilla-models';
import type EnumLookupValue from './EnumLookupValue.js';

const EnumLookupValueModel = m
  .object<EnumLookupValue>('EnumLookupValue')
  .property('key', m.optional(Model))
  .property('label', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type EnumLookupValueModel = typeof EnumLookupValueModel;

export default EnumLookupValueModel;
