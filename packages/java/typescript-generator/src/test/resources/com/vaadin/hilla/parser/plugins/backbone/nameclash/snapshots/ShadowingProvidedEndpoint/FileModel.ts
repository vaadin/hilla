import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
import type File from './File.js';

class FileModel<T extends File = File> extends ObjectModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(FileModel);

  get name(): StringModel {
    return this[_getPropertyModel]('name', (parent, key) =>
      new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
  }
}

export default FileModel;
