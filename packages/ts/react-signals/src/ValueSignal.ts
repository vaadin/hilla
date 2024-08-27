import { createReplaceStateEvent, type StateEvent } from './events.js';
import { $processServerResponse, $update, FullStackSignal } from './FullStackSignal.js';

type PromiseWithResolvers = ReturnType<typeof Promise.withResolvers<void>>;
type PendingRequestsRecord<T> = Readonly<{
  waiter: PromiseWithResolvers;
  callback(value: T): T;
  canceled: boolean;
}>;

/**
 * An operation subscription that can be canceled.
 */
export interface OperationSubscription {
  cancel(): void;
}

/**
 * A full-stack signal that holds an arbitrary value.
 */
export class ValueSignal<T> extends FullStackSignal<T> {
  readonly #pendingRequests = new Map<string, PendingRequestsRecord<T>>();

  /**
   * Sets the value.
   * Note that the value change event that is propagated to the server as the
   * result of this operation is not taking the last seen value into account and
   * will overwrite the shared value on the server unconditionally (AKA: "Last
   * Write Wins"). If you need to perform a conditional update, use the
   * `replace` method instead.
   *
   * @param value - The new value.
   */
  set(value: T): void {
    this.value = value;
  }

  /**
   * Replaces the value with a new one only if the current value is equal to the
   * expected value.
   *
   * @param expected - The expected value.
   * @param newValue - The new value.
   */
  replace(expected: T, newValue: T): void {
    this[$update](createReplaceStateEvent(expected, newValue));
  }

  /**
   * Tries to update the value by applying the callback function to the current
   * value. In case of a concurrent change, the callback is run again with an
   * updated input value. This is repeated until the result can be applied
   * without concurrent changes, or the operation is canceled.
   *
   * Note that there is no guarantee that cancel() will be effective always,
   * since a succeeding operation might already be on its way to the server.
   *
   * @param callback - The function that is applied on the current value to
   *                   produce the new value.
   * @returns An operation subscription that can be canceled.
   */
  update(callback: (value: T) => T): OperationSubscription {
    const newValue = callback(this.value);
    const event = createReplaceStateEvent(this.value, newValue);
    this[$update](event);
    const waiter = Promise.withResolvers<void>();
    const pendingRequest = { callback, waiter, canceled: false };
    this.#pendingRequests.set(event.id, pendingRequest);
    return {
      cancel: () => {
        pendingRequest.canceled = true;
        pendingRequest.waiter.resolve();
      },
    };
  }

  protected override [$processServerResponse](event: StateEvent<T>): void {
    const record = this.#pendingRequests.get(event.id);

    if (event.type === 'reject' && record) {
      if (record.canceled) {
        this.#pendingRequests.delete(event.id);
        return;
      }
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.update(record.callback);
    }

    if (event.type === 'snapshot') {
      if (record) {
        record.waiter.resolve();
        this.#pendingRequests.delete(event.id);
      }

      this.value = event.value;
    }
  }
}
