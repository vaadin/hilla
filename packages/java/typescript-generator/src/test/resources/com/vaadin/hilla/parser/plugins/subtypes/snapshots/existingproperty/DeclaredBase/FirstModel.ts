import m, { StringModel } from '@vaadin/hilla-models';
import DeclaredBaseModel from '../DeclaredBaseModel.js';
import type First from './First.js';

const FirstModel = m
  .extend(DeclaredBaseModel)
  .object<First>('First')
  .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('kind', m.literal('first'))
  .build();
type FirstModel = typeof FirstModel;

export default FirstModel;
