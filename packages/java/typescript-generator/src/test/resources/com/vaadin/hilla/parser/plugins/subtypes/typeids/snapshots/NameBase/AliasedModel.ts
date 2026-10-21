import m, { StringModel } from '@vaadin/hilla-models';
import NameBaseModel from '../NameBaseModel.js';
import type Aliased from './Aliased.js';

const AliasedModel = m
  .extend(NameBaseModel)
  .object<Aliased>('Aliased')
  .property('alias', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('@type', m.literal('aliased'))
  .build();
type AliasedModel = typeof AliasedModel;

export default AliasedModel;
