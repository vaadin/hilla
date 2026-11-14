import m, { StringModel } from '@vaadin/hilla-models';
import BitterModel from './BitterModel.js';
import type Roasted from './Roasted.js';

const RoastedModel = m
  .extend(BitterModel)
  .object<Roasted>('Roasted')
  .property('origin', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type RoastedModel = typeof RoastedModel;

export default RoastedModel;
