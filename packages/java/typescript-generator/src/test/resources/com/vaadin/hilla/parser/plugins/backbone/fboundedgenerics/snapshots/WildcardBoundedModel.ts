import m, { Model } from '@vaadin/hilla-models';
import type WildcardBounded from './WildcardBounded.js';

const WildcardBoundedModel = m
  .object<WildcardBounded>('WildcardBounded')
  .property('value', m.optional(Model))
  .build();
type WildcardBoundedModel = typeof WildcardBoundedModel;

export default WildcardBoundedModel;
