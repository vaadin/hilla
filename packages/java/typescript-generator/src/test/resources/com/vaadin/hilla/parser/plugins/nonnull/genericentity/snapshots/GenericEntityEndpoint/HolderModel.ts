import m from '@vaadin/hilla-models';
import BoxModel from './BoxModel.js';
import type Holder from './Holder.js';

const HolderModel = m
  .object<Holder>('Holder')
  .property('boxOfMaybe', BoxModel)
  .property('maybeBoxOf', m.optional(BoxModel))
  .build();
type HolderModel = typeof HolderModel;

export default HolderModel;
