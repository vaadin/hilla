import m, { Model } from '@vaadin/hilla-models';
import type Holder from './Holder.js';

const HolderModel = m
  .object<Holder>('Holder')
  .property('value', m.optional(Model))
  .property('maybe', m.optional(Model))
  .build();
type HolderModel = typeof HolderModel;

export default HolderModel;
