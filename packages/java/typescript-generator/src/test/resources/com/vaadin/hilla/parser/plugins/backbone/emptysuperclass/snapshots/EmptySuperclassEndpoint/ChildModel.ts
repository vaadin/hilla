import m, { StringModel } from '@vaadin/hilla-models';
import type Child from './Child.js';
import EmptyModel from './EmptyModel.js';

const ChildModel = m
  .extend(EmptyModel)
  .object<Child>('Child')
  .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type ChildModel = typeof ChildModel;

export default ChildModel;
