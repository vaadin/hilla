import m, { StringModel } from '@vaadin/hilla-models';
import EnumDiscriminatedModel from '../EnumDiscriminatedModel.js';
import type Bitter from './Bitter.js';
import TasteModel from './TasteModel.js';

const BitterModel = m
  .extend(EnumDiscriminatedModel)
  .object<Bitter>('Bitter')
  .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('taste', m.optional(TasteModel))
  .build();
type BitterModel = typeof BitterModel;

export default BitterModel;
