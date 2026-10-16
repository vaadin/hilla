import { EnumModel, _enum, makeEnumEmptyValueCreator } from '@vaadin/hilla-lit-form';
import Kind from './Kind.js';

class KindModel extends EnumModel<typeof Kind> {
  static override createEmptyValue = makeEnumEmptyValueCreator(KindModel);

  readonly [_enum] = Kind;
}

export default KindModel;
