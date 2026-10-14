import { EnumModel, _enum, makeEnumEmptyValueCreator } from '@vaadin/hilla-lit-form';
import Level from './Level.js';

class LevelModel extends EnumModel<typeof Level> {
  static override createEmptyValue = makeEnumEmptyValueCreator(LevelModel);

  readonly [_enum] = Level;
}

export default LevelModel;
