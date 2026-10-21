import m, { NumberModel } from '@vaadin/hilla-models';
import EnumDiscriminatedModel from '../EnumDiscriminatedModel.js';
import type Salty from './Salty.js';

const SaltyModel = m
  .extend(EnumDiscriminatedModel)
  .object<Salty>('Salty')
  .property('level', m.meta(NumberModel, { jvmType: 'int' }))
  .property('taste', m.literal('SALTY'))
  .build();
type SaltyModel = typeof SaltyModel;

export default SaltyModel;
