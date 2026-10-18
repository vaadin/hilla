import m, { StringModel } from '@vaadin/hilla-models';
import NameBaseModel from '../NameBaseModel.js';
import type Listed from './Listed.js';

const ListedModel = m
  .extend(NameBaseModel)
  .object<Listed>('Listed')
  .property('tag', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('@type', m.literal('listed'))
  .build();
type ListedModel = typeof ListedModel;

export default ListedModel;
