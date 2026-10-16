import m, { StringModel } from '@vaadin/hilla-models';
import type FromSuperclass from './FromSuperclass.js';
import NamedParentModel from './NamedParentModel.js';

const FromSuperclassModel = m
  .extend(NamedParentModel)
  .object<FromSuperclass>('FromSuperclass')
  .property('number', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('@type', m.literal('from-superclass'))
  .build();
type FromSuperclassModel = typeof FromSuperclassModel;

export default FromSuperclassModel;
