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

/**
 * Generates a random base64-encoded string of the specified size in bytes.
 * @param sizeBytes - The size of the random string in bytes. Default is 8 bytes.
 * @returns A base64-encoded string.
 */
export function randomId(sizeBytes = 8): string {
  const bytes = new Uint8Array(sizeBytes);
  crypto.getRandomValues(bytes);
  let binary = '';
  for (const value of bytes) {
    binary += String.fromCharCode(value);
  }
  const base64 = btoa(binary);
  let end = base64.length;
  while (end > 0 && base64[end - 1] === '=') {
    end -= 1;
  }
  return base64.slice(0, end);
}
