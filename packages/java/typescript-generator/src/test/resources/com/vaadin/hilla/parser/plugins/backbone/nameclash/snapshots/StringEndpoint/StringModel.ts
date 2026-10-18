import m, { StringModel as StringModel_1 } from '@vaadin/hilla-models';
import type String from './String.js';

const StringModel = m
  .object<String>('String')
  .property('value', m.meta(m.optional(StringModel_1), { jvmType: 'java.lang.String' }))
  .build();
type StringModel = typeof StringModel;

export default StringModel;
