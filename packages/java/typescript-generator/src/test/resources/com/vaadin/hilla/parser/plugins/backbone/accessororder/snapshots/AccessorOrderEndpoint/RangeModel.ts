import m, { NumberModel } from '@vaadin/hilla-models';
import type Range from './Range.js';

const RangeModel = m
  .object<Range>('Range')
  .property('upper', m.meta(NumberModel, { jvmType: 'int' }))
  .property('lower', m.meta(NumberModel, { jvmType: 'int' }))
  .build();
type RangeModel = typeof RangeModel;

export default RangeModel;
