import { computed, type ReadonlySignal, Signal } from './core.js';

declare class Computed<T> extends Signal<T> implements ReadonlySignal<T> {
  constructor(compute: () => T);
}

function Computed<T>(this: Computed<T>, compute: () => T) {
  // Replica of the private Computed constructor
  Signal.call(this, undefined);

  const anyThis = this as any;
  // _compute
  anyThis.x = compute;
  // _sources
  anyThis.s = undefined;
  // _globalVersion
  anyThis.g =  - 1;
  // _flags = OUTDATED
  anyThis.f = 1 << 2;
}

// Create dummy real Computed to be able to get its prototype
Computed.prototype = (computed(() => {}) as any).__proto__;

export abstract class SharedSignal<T> extends Computed<T> {
  protected constructor(compute: () => T) {
    super(compute);
  }
}
