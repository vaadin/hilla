import m, { StringModel } from '@vaadin/hilla-models';
import type File from './File.js';

const FileModel = m
  .object<File>('File')
  .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type FileModel = typeof FileModel;

export default FileModel;
