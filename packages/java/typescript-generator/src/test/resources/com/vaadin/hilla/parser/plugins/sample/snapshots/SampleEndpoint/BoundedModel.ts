import m from '@vaadin/hilla-models';
import type Bounded from './Bounded.js';
import SampleModel from './SampleModel.js';

const BoundedModel = m
  .object<Bounded>('Bounded')
  .property('held', m.optional(SampleModel))
  .build();
type BoundedModel = typeof BoundedModel;

export default BoundedModel;
