import m from '@vaadin/hilla-models';
import FigureModel from '../FigureModel.js';
import type Blank from './Blank.js';

const BlankModel = m
  .extend(FigureModel)
  .object<Blank>('Blank')
  .property('@type', m.literal('blank'))
  .build();
type BlankModel = typeof BlankModel;

export default BlankModel;
