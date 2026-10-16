import m from '@vaadin/hilla-models';
import ShadedModel from '../ShadedModel.js';
import type Pale from './Pale.js';

const PaleModel = m
  .extend(ShadedModel)
  .object<Pale>('Pale')
  .property('shade', m.literal('pale'))
  .build();
type PaleModel = typeof PaleModel;

export default PaleModel;
