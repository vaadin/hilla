import type EnumDiscriminated from '../EnumDiscriminated.js';

interface Salty extends EnumDiscriminated {
  level: number;
  taste: 'SALTY';
}

export default Salty;
