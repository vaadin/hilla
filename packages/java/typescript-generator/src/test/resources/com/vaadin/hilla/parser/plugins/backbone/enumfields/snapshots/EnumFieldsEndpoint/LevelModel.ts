import m from '@vaadin/hilla-models';
import Level from './Level.js';

const LevelModel = m.enum(Level, 'Level');
type LevelModel = typeof LevelModel;

export default LevelModel;
