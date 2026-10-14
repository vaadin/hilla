import m from '@vaadin/hilla-models';
import type Count from './Count.js';

const CountModel = m
  .object<Count>('Count')
  .build();
type CountModel = typeof CountModel;

export default CountModel;
