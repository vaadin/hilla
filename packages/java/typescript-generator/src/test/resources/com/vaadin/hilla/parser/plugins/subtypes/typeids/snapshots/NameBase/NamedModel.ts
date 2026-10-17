import m, { StringModel } from '@vaadin/hilla-models';
import NameBaseModel from '../NameBaseModel.js';
import type Named from './Named.js';

const NamedModel = m
  .extend(NameBaseModel)
  .object<Named>('Named')
  .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('@type', m.literal('named'))
  .build();
type NamedModel = typeof NamedModel;

export default NamedModel;
