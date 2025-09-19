import { FullStackSignal, getNode } from './FullStackSignal.js';
import { NodeSignal } from './NodeSignal.js';

export class ListSignal<T> extends FullStackSignal {
  get value(): readonly T[] {
    return getNode(this).listChildren;
  }

  asNode(): NodeSignal {
    return new NodeSignal(this);
  }
}
