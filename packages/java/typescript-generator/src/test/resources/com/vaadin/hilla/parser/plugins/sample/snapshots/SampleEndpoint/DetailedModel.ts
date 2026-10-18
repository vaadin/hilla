import m, { StringModel } from '@vaadin/hilla-models';
import type Detailed from './Detailed.js';
import SampleModel from './SampleModel.js';

const DetailedModel = m
  .extend(SampleModel)
  .object<Detailed>('Detailed')
  .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .property('basis', m.optional(SampleModel))
  .build();
type DetailedModel = typeof DetailedModel;

export default DetailedModel;
