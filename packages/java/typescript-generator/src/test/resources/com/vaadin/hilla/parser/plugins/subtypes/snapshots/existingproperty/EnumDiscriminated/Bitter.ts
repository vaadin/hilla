import type EnumDiscriminated from '../EnumDiscriminated.js';
import type Taste from './Taste.js';

interface Bitter extends EnumDiscriminated {
  note?: string;
  taste?: Taste;
}

export default Bitter;
