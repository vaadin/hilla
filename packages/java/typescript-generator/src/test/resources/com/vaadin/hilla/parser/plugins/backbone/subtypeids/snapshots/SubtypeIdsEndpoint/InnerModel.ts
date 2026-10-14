import m, { StringModel } from '@vaadin/hilla-models';
import type Inner from './Inner.js';
import WrappedModel from './WrappedModel.js';

const InnerModel = m
  .extend(WrappedModel)
  .object<Inner>('Inner')
  .property('value', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type InnerModel = typeof InnerModel;

export default InnerModel;
