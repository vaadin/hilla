import type DeclaredBase from '../DeclaredBase.js';

interface First extends DeclaredBase {
  note?: string;
  kind: 'first';
}

export default First;
