import m, { NumberModel } from '@vaadin/hilla-models';
import type Ring from './Ring.js';
import RoundModel from './RoundModel.js';

const RingModel = m
  .extend(RoundModel)
  .object<Ring>('Ring')
  .property('hole', m.meta(NumberModel, { jvmType: 'double' }))
  .property('@type', m.literal('ring'))
  .build();
type RingModel = typeof RingModel;

export default RingModel;
