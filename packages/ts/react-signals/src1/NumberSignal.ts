import { FullStackSignal, getNode } from './FullStackSignal.js';
import { NodeSignal } from './NodeSignal.js';

export class NumberSignal<T extends bigint | number = number> extends FullStackSignal {
  value(): T {
    return getNode(this).value as T;
  }

  asNode(): NodeSignal {
    return new NodeSignal(this);
  }
}
