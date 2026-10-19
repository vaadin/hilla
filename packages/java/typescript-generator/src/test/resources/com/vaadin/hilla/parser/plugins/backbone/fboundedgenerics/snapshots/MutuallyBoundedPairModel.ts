import m, { Model } from '@vaadin/hilla-models';
import type MutuallyBoundedPair from './MutuallyBoundedPair.js';

const MutuallyBoundedPairModel = m
  .object<MutuallyBoundedPair>('MutuallyBoundedPair')
  .property('left', m.optional(Model))
  .property('right', m.optional(Model))
  .build();
type MutuallyBoundedPairModel = typeof MutuallyBoundedPairModel;

export default MutuallyBoundedPairModel;
