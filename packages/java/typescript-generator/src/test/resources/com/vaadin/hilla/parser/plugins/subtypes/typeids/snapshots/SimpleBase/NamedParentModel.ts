import m, { NumberModel } from '@vaadin/hilla-models';
import SimpleBaseModel from '../SimpleBaseModel.js';
import type NamedParent from './NamedParent.js';

const NamedParentModel = m
  .extend(SimpleBaseModel)
  .object<NamedParent>('NamedParent')
  .property('size', m.meta(NumberModel, { jvmType: 'int' }))
  .build();
type NamedParentModel = typeof NamedParentModel;

export default NamedParentModel;
