export { signal, computed, effect, batch, untracked, Signal, type ReadonlySignal } from '@preact/signals-react';
export { useSignal, useComputed, useSignalEffect } from '@preact/signals-react';

export type SignalMethodOptions<T> = Readonly<{
  defaultValue: T;
}>;
