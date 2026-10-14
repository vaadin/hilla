import m from '@vaadin/hilla-models';
import type Wrapped from './Wrapped.js';

const WrappedModel = m
  .object<Wrapped>('Wrapped')
  .build();
type WrappedModel = typeof WrappedModel;

export default WrappedModel;
