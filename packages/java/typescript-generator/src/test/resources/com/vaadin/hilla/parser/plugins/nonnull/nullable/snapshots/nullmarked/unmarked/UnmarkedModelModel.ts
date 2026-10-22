import m, { StringModel } from '@vaadin/hilla-models';
import type UnmarkedModel from './UnmarkedModel.js';

const UnmarkedModelModel = m
  .object<UnmarkedModel>('UnmarkedModel')
  .property('value', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type UnmarkedModelModel = typeof UnmarkedModelModel;

export default UnmarkedModelModel;
