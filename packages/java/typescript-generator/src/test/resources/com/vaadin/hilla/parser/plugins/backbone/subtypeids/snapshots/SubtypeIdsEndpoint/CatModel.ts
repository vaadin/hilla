import m, { BooleanModel } from '@vaadin/hilla-models';
import type Cat from './Cat.js';
import PetModel from './PetModel.js';

const CatModel = m
  .extend(PetModel)
  .object<Cat>('Cat')
  .property('indoor', m.meta(BooleanModel, { jvmType: 'boolean' }))
  .property('@type', m.literal('cat'))
  .build();
type CatModel = typeof CatModel;

export default CatModel;
