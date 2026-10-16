import m, { NumberModel } from '@vaadin/hilla-models';
import FigureModel from '../FigureModel.js';
import type Round from './Round.js';

const RoundModel = m
  .extend(FigureModel)
  .object<Round>('Round')
  .property('radius', m.meta(NumberModel, { jvmType: 'double' }))
  .property('@type', m.union(m.literal('round'), m.literal('ring')))
  .build();
type RoundModel = typeof RoundModel;

export default RoundModel;
