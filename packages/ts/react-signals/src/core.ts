export { signal, computed, effect, batch, untracked, Signal, type ReadonlySignal } from '@preact/signals-core';

export type SignalMethodOptions<T> = Readonly<{
  defaultValue: T;
}>;
