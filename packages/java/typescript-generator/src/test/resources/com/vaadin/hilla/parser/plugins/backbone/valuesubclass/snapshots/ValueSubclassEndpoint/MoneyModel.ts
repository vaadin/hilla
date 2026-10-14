import m from '@vaadin/hilla-models';
import type Money from './Money.js';

const MoneyModel = m
  .object<Money>('Money')
  .build();
type MoneyModel = typeof MoneyModel;

export default MoneyModel;
