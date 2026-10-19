import m, { StringModel } from '@vaadin/hilla-models';
import type EnumDiscriminated from './EnumDiscriminated.js';

const EnumDiscriminatedModel = m
  .object<EnumDiscriminated>('EnumDiscriminated')
  .property('label', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type EnumDiscriminatedModel = typeof EnumDiscriminatedModel;

export default EnumDiscriminatedModel;
