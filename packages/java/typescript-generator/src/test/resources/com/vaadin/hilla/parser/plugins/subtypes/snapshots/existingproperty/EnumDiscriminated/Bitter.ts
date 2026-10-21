import type EnumDiscriminated from '../EnumDiscriminated.js';

interface Bitter extends EnumDiscriminated {
  note?: string;
  taste: 'BITTER' | 'VERY_BITTER';
}

export default Bitter;
