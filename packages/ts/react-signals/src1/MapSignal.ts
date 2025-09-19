import { FullStackSignal, getNode } from './FullStackSignal.js';
import { NodeSignal } from './NodeSignal.js';

export class MapSignal<T> extends FullStackSignal {
  get value(): Readonly<Record<string, T>> {
    return getNode(this).mapChildren;
  }

  asNode(): NodeSignal {
    return new NodeSignal(this);
  }
}
