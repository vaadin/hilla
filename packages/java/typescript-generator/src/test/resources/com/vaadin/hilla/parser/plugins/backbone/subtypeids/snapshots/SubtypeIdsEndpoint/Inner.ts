import type Wrapped from './Wrapped.js';

interface Inner extends Wrapped {
  value?: string;
}

export default Inner;
