import m, { StringModel } from '@vaadin/hilla-models';
import InheritedBaseModel from '../InheritedBaseModel.js';
import type Sweet from './Sweet.js';

const SweetModel = m
  .extend(InheritedBaseModel)
  .object<Sweet>('Sweet')
  .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('flavour', m.literal('sweet'))
  .build();
type SweetModel = typeof SweetModel;

export default SweetModel;
