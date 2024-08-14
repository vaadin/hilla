import { signal } from '@preact/signals-react';
import { Signal } from './core.js';
import type { SignalChannel } from './SignalChannel.js';

export type ValueSignalOptions = Readonly<{
  channel?: SignalChannel;
}>;

/**
 * A signal that holds a value. The underlying
 * value of this signal is stored and updated as a
 * shared value on the server.
 *
 * @internal
 */
export class ValueSignal<T = unknown> extends Signal<T> {
  readonly #pending = signal(false);
  readonly #error = signal<Error | undefined>(undefined);

  constructor(value?: T, options?: ValueSignalOptions) {
    super(value);
    options?.channel?.connect(this);
  }

  /**
   * Defines whether the signal is currently pending server-side response.
   */
  get pending(): boolean {
    return this.#pending.value;
  }

  /**
   * Defines whether the signal has an error.
   */
  get error(): Error | undefined {
    return this.#error.value;
  }

  override subscribe(connector: (value: T, done: (promise: Promise<void>) => void) => void): () => void {
    return super.subscribe((value) => {
      connector(value, (promise) => {
        promise
          .catch((error: unknown) => {
            this.#error.value = error instanceof Error ? error : new Error(String(error));
          })
          .finally(() => {
            this.#pending.value = false;
          });
      });
    });
  }
}

/**
 * A signal that holds a number value. The underlying
 * value of this signal is stored and updated as a
 * shared value on the server.
 *
 * After obtaining the NumberSignal instance from
 * a server-side service that returns one, the value
 * can be updated using the `value` property,
 * and it can be read with or without the
 * `value` property (similar to a normal signal):
 *
 * @example
 * ```tsx
 *  const counter = CounterService.counter();
 *
 * return (
 *    <Button onClick={() => counter++)}>
 *      Click count: { counter }
 *    </Button>
 *    <Button onClick={() => counter.value = 0}>Reset</Button>
 * );
 * ```
 */
export class NumberSignal extends ValueSignal<number> {}
