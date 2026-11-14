import type Bitter from './EnumDiscriminated/Bitter.js';
import type VeryBitter from './EnumDiscriminated/VeryBitter.js';

type EnumDiscriminatedUnion = (Bitter & { taste: 'BITTER' }) | VeryBitter;

export default EnumDiscriminatedUnion;
