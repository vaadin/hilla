import m, { Model, NumberModel } from '@vaadin/hilla-models';
import type AbstractEntity from './AbstractEntity.js';

const AbstractEntityModel = m
  .object<AbstractEntity>('AbstractEntity')
  .property('version', m.meta(NumberModel, { jvmType: 'int' }))
  .property('id', m.optional(Model))
  .build();
type AbstractEntityModel = typeof AbstractEntityModel;

export default AbstractEntityModel;
