import m, { StringModel } from '@vaadin/hilla-models';
import ConfiguredKindBaseModel from './ConfiguredKindBaseModel.js';
import type ConfiguredKindSub from './ConfiguredKindSub.js';

const ConfiguredKindSubModel = m
  .extend(ConfiguredKindBaseModel)
  .object<ConfiguredKindSub>('ConfiguredKindSub')
  .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type ConfiguredKindSubModel = typeof ConfiguredKindSubModel;

export default ConfiguredKindSubModel;
