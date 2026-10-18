import type NamedParent from './NamedParent.js';

interface FromInterface extends NamedParent {
  address?: string;
  '@type': 'from-interface';
}

export default FromInterface;
