import type EnumDiscriminated from '../EnumDiscriminated.js';

interface Bitter extends EnumDiscriminated {
  note?: string;
  taste: 'BITTER';
}

export default Bitter;
