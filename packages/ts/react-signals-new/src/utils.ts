import type { ReadonlySignal } from '@preact/signals-react';

export async function createPromiseFromSignal<T, U, E>(
  signal: ReadonlySignal<T>,
  callback: (value: T, resolve: (value: PromiseLike<U> | U) => void, reject: (reason?: E) => void) => void,
): Promise<U> {
  return new Promise((resolve, reject) => {
    const unsubscribe = signal.subscribe((value) => {
      if (!value) {
        return;
      }

      unsubscribe();
      callback(value, resolve, reject);
    });
  });
}
