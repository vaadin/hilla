import m from '@vaadin/hilla-models';
import type ConfiguredKindBase from './ConfiguredKindBase.js';
import KindModel from './KindModel.js';

const ConfiguredKindBaseModel = m
  .object<ConfiguredKindBase>('ConfiguredKindBase')
  .property('kind', m.optional(KindModel))
  .build();
type ConfiguredKindBaseModel = typeof ConfiguredKindBaseModel;

export default ConfiguredKindBaseModel;
