import m, { NumberModel, StringModel } from '@vaadin/hilla-models';
import type Annotated from './Annotated.js';

const AnnotatedModel = m
  .object<Annotated>('Annotated')
  .property('count', m.meta(NumberModel, { jvmType: 'int' }))
  .property('words', m.meta(m.array(m.meta(StringModel, { jvmType: 'java.lang.String' })), { jvmType: 'java.lang.String[]' }))
  .build();
type AnnotatedModel = typeof AnnotatedModel;

export default AnnotatedModel;
