import { type OperationSubscription, ValueSignal } from './ValueSignal.js';

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
export class NumberSignal extends ValueSignal<number> {
  /**
   * Increments the value by the provided delta. The delta can be negative. If
   * no delta is provided, the value is incremented by 1.
   * This operation is retried in case of a concurrent change. If you want to
   * avoid this, pass `false` to the `retryOnFailure` parameter.
   *
   * @param delta - The delta to increment the value by. The delta can be
   * negative. Defaults to 1.
   */
  increment(delta: number = 1): OperationSubscription {
    return this.update((value) => value + delta);
  }

  /**
   * Adds the provided delta to the value. The delta can be negative.
   * @param delta - The delta to add to the value. The delta can be negative.
   */
  add(delta: number): OperationSubscription {
    return this.increment(delta);
  }
}
