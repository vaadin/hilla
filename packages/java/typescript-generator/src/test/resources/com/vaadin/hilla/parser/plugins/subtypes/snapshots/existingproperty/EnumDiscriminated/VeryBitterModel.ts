import m, { NumberModel } from '@vaadin/hilla-models';
import RoastedModel from './RoastedModel.js';
import type VeryBitter from './VeryBitter.js';

const VeryBitterModel = m
  .extend(RoastedModel)
  .object<VeryBitter>('VeryBitter')
  .property('level', m.meta(NumberModel, { jvmType: 'int' }))
  .property('taste', m.literal('VERY_BITTER'))
  .build();
type VeryBitterModel = typeof VeryBitterModel;

export default VeryBitterModel;
