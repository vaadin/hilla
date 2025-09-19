import { FullStackSignal } from './FullStackSignal.js';
import { ListSignal } from './ListSignal.js';
import { MapSignal } from './MapSignal.js';
import { NumberSignal } from './NumberSignal.js';
import { ValueSignal } from './ValueSignal.js';

export class NodeSignal extends FullStackSignal {
  asValue<T>(): ValueSignal<T> {
    return new ValueSignal<T>(this);
  }
  asNumber(): NumberSignal {
    return new NumberSignal(this);
  }
  asList<T>(): ListSignal<T> {
    return new ListSignal<T>(this);
  }
  asMap<T>(): MapSignal<T> {
    return new MapSignal<T>(this);
  }
}
