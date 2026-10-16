import m, { StringModel } from '@vaadin/hilla-models';
import type Pet from './Pet.js';

const PetModel = m
  .object<Pet>('Pet')
  .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type PetModel = typeof PetModel;

export default PetModel;
