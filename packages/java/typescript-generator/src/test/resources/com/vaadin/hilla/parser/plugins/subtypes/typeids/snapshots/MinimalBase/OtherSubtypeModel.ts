import m, { NumberModel } from '@vaadin/hilla-models';
import MinimalBaseModel from '../MinimalBaseModel.js';
import type OtherSubtype from './OtherSubtype.js';

const OtherSubtypeModel = m
  .extend(MinimalBaseModel)
  .object<OtherSubtype>('OtherSubtype')
  .property('size', m.meta(NumberModel, { jvmType: 'int' }))
  .build();
type OtherSubtypeModel = typeof OtherSubtypeModel;

export default OtherSubtypeModel;
