import m, { Model } from '@vaadin/hilla-models';
import type Wrapper from './Wrapper.js';

const WrapperModel = m
  .object<Wrapper>('Wrapper')
  .property('value', m.optional(Model))
  .build();
type WrapperModel = typeof WrapperModel;

export default WrapperModel;
