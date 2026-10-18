import m, { StringModel } from '@vaadin/hilla-models';
import SimpleBaseModel from '../SimpleBaseModel.js';
import type Nested from './Nested.js';

const NestedModel = m
  .extend(SimpleBaseModel)
  .object<Nested>('Nested')
  .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('@type', m.literal('Nested'))
  .build();
type NestedModel = typeof NestedModel;

export default NestedModel;
