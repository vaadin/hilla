import m, { StringModel } from '@vaadin/hilla-models';
import NameBaseModel from '../NameBaseModel.js';
import type Nested from './Nested.js';

const NestedModel = m
  .extend(NameBaseModel)
  .object<Nested>('Nested')
  .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('@type', m.literal('NameBase$Nested'))
  .build();
type NestedModel = typeof NestedModel;

export default NestedModel;
