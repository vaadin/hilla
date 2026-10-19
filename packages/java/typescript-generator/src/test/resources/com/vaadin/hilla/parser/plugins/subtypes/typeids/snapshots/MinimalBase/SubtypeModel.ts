import m, { StringModel } from '@vaadin/hilla-models';
import MinimalBaseModel from '../MinimalBaseModel.js';
import type Subtype from './Subtype.js';

const SubtypeModel = m
  .extend(MinimalBaseModel)
  .object<Subtype>('Subtype')
  .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type SubtypeModel = typeof SubtypeModel;

export default SubtypeModel;
