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
   * Increments the value by the specified delta. The delta can be negative to
   * decrease the value. If no delta is provided, the value increases by 1.
   *
   * This method differs from using the `++` or `+=` operators directly on the
   * signal instance. It performs an atomic operation to prevent conflicts from
   * concurrent changes, ensuring that other users' modifications are not
   * accidentally overwritten.
   *
   * By default, if there are concurrent changes on the server, this operation
   * will keep retrying until it succeeds. The returned `OperationSubscription`
   * can be used to cancel the operation, if needed.
   * To disable automatic retries, set `retryOnFailure` to `false`. Note that
   * when `retryOnFailure` is `false`, the returned `OperationSubscription` will
   * not be cancellable.
   *
   * @remarks
   * **IMPORTANT:** please note that if the operation has already succeeded on
   * the server, calling `cancel` will not prevent or undo the operation.The
   * `cancel` method only stops further retry attempts if a previous attempt was
   * rejected by the server.
   *
   * @param delta - The delta to increment the value by. The delta can be
   * negative. Defaults to 1.
   * @param retryOnFailure - Whether to retry the operation in case of a
   * concurrent value change on the server or not. Defaults to `true`.
   *
   * @returns An object that can be used to cancel the operation.
   */
  increment(delta: number = 1, retryOnFailure: boolean = true): OperationSubscription {
    const noopCancelSubscription = { cancel: () => {} };
    if (delta === 0) {
      return noopCancelSubscription;
    }
    if (!retryOnFailure) {
      const expected = this.value;
      this.replace(expected, expected + delta);
      return noopCancelSubscription;
    }
    return this.update((value) => value + delta);
  }

  /**
   * Adds the provided delta to the value. The delta can be negative to perform
   * a subtraction.
   * This operation does not retry in case failure due to concurrent value
   * changes from other clients have already applied to the shared value on the
   * server. If you want the operation to be retried until it succeeds, use the
   * overload with retry functionality.
   *
   * @param delta - The delta to add to the value. The delta can be negative to
   * perform a subtraction.
   * @see {@link NumberSignal.add|add(delta: number, retryOnFailure: boolean)}
   */
  add(delta: number): void;

  /**
   * Adds the provided delta to the value. The delta can be negative to perform
   * a subtraction.
   * If `true` passed to the `retryOnFailure` parameter, the operation is
   * retried in case of failures due to concurrent value changes on the server
   * until it succeeds. Note that the returned operation subscription can be
   * used to cancel the operation.
   *
   * @param delta - The delta to add to the value. The delta can be negative.
   * @param retryOnFailure - Whether to retry the operation in case of failures
   * due to concurrent value changes on the server or not.
   * @returns An operation subscription that can be canceled.
   */
  add(delta: number, retryOnFailure: boolean): OperationSubscription;

  add(delta: number, retryOnFailure?: boolean): OperationSubscription | void {
    if (delta !== 0) {
      if (retryOnFailure) {
        return this.increment(delta, retryOnFailure);
      }
      const expected = this.value;
      this.replace(expected, expected + delta);
    }
    // eslint-disable-next-line no-useless-return, consistent-return
    return;
  }
}
