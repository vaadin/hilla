import type NameBase from '../NameBase.js';

interface Nested extends NameBase {
  note?: string;
  '@type': 'NameBase$Nested';
}

export default Nested;
