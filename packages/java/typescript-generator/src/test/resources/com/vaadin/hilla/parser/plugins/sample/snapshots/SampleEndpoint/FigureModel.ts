import m, { StringModel } from '@vaadin/hilla-models';
import type Figure from './Figure.js';

const FigureModel = m
  .object<Figure>('Figure')
  .property('label', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type FigureModel = typeof FigureModel;

export default FigureModel;
