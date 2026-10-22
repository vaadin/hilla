import type SimpleBase from '../SimpleBase.js';

interface Nested extends SimpleBase {
  note?: string;
  '@type': 'Nested';
}

export default Nested;
