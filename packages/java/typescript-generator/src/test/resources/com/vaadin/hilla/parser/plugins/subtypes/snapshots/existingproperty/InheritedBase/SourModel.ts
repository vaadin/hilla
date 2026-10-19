import m, { NumberModel } from '@vaadin/hilla-models';
import InheritedBaseModel from '../InheritedBaseModel.js';
import type Sour from './Sour.js';

const SourModel = m
  .extend(InheritedBaseModel)
  .object<Sour>('Sour')
  .property('level', m.meta(NumberModel, { jvmType: 'int' }))
  .property('flavour', m.literal('sour'))
  .build();
type SourModel = typeof SourModel;

export default SourModel;
