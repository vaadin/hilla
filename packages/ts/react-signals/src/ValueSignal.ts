import { createReplaceStateEvent, type StateEvent } from './events.js';
import { $processServerResponse, $update, FullStackSignal } from './FullStackSignal.js';

/**
 * A return type for signal operations.
 */
export type Operation = {
  result: {
    then(callback: () => void): Operation['result'];
  };
};

/**
 * An operation where all callbacks are predefined to be no-ops.
 */
export const noOperation: Operation = Object.freeze({
  result: {
    then(_callback: () => void): Operation['result'] {
      return noOperation.result;
    },
  },
});

export const createOperation = (event: StateEvent<unknown>): Operation => {
  const op: Operation = {
    result: {
      then(callback: () => void) {
        event.thenCallback = callback;
        return op.result;
      },
    },
  };
  return op;
};

type PromiseWithResolvers = ReturnType<typeof Promise.withResolvers<void>>;
type PendingRequestsRecord<T> = Readonly<{
  waiter: PromiseWithResolvers;
  callback(value: T): T;
  canceled: boolean;
}>;

/**
 * An operation subscription that can be canceled.
 */
export interface OperationSubscription extends Operation {
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
   * @returns An operation object that allows to perform additional actions.
   */
  replace(expected: T, newValue: T): Operation {
    const event = createReplaceStateEvent(expected, newValue);
    this[$update](event);
    return createOperation(event);
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
   * @returns An operation object that allows to perform additional actions, including cancellation.
   */
  update(callback: (value: T) => T): OperationSubscription {
    const newValue = callback(this.value);
    const event = createReplaceStateEvent(this.value, newValue);
    this[$update](event);
    const waiter = Promise.withResolvers<void>();
    const pendingRequest = { callback, waiter, canceled: false };
    this.#pendingRequests.set(event.id, pendingRequest);
    return {
      ...createOperation(event),
      cancel: () => {
        pendingRequest.canceled = true;
        pendingRequest.waiter.resolve();
      },
    };
  }

  protected override [$processServerResponse](event: StateEvent<T>): void {
    const record = this.#pendingRequests.get(event.id);
    if (record) {
      this.#pendingRequests.delete(event.id);

      if (!(event.accepted || !record.canceled)) {
        this.update(record.callback);
      }
    }

    if (event.accepted || event.type === 'snapshot') {
      record?.waiter.resolve();
      this.#applyAcceptedEvent(event);
      event.thenCallback?.();
    }
  }

  #applyAcceptedEvent(event: StateEvent<T>): void {
    if (event.type === 'set' || event.type === 'snapshot') {
      this.value = event.value;
    } else if (event.type === 'replace') {
      if (JSON.stringify(this.value) === JSON.stringify(event.expected)) {
        this.value = event.value;
      }
    }
  }
}
