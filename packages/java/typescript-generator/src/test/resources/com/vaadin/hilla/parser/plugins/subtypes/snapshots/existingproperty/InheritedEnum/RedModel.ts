import m, { StringModel } from '@vaadin/hilla-models';
import InheritedEnumModel from '../InheritedEnumModel.js';
import type Red from './Red.js';

const RedModel = m
  .extend(InheritedEnumModel)
  .object<Red>('Red')
  .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type RedModel = typeof RedModel;

export default RedModel;
