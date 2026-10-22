import type NamedParent from './NamedParent.js';

interface FromSuperclass extends NamedParent {
  number?: string;
  '@type': 'from-superclass';
}

export default FromSuperclass;
