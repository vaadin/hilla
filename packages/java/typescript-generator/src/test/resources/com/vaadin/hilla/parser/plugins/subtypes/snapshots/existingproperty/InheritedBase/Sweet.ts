import type InheritedBase from '../InheritedBase.js';

interface Sweet extends InheritedBase {
  note?: string;
  flavour: 'sweet';
}

export default Sweet;
