import type NameBase from '../NameBase.js';

interface Aliased extends NameBase {
  alias?: string;
  '@type': 'aliased';
}

export default Aliased;
