import m, { StringModel } from '@vaadin/hilla-models';
import type FromInterface from './FromInterface.js';
import NamedParentModel from './NamedParentModel.js';

const FromInterfaceModel = m
  .extend(NamedParentModel)
  .object<FromInterface>('FromInterface')
  .property('address', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('@type', m.literal('from-interface'))
  .build();
type FromInterfaceModel = typeof FromInterfaceModel;

export default FromInterfaceModel;
