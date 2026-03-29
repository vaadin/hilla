// Side-effect import ensures React integration (Signal.prototype patches) is loaded
// eslint-disable-next-line import/no-unassigned-import
import '@preact/signals-react/runtime';

export { signal, computed, effect, batch, untracked, Signal, type ReadonlySignal } from '@preact/signals-react';
export { useSignal, useComputed, useSignalEffect } from '@preact/signals-react';

export type SignalMethodOptions<T> = Readonly<{
  defaultValue: T;
}>;
