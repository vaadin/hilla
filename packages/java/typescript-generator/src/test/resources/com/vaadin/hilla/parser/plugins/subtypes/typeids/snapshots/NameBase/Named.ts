import type NameBase from '../NameBase.js';

interface Named extends NameBase {
  name?: string;
  '@type': 'named';
}

export default Named;
