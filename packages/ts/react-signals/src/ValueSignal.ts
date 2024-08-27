import { createReplaceStateEvent, type StateEvent } from './events.js';
import { $processServerResponse, $update, FullStackSignal } from './FullStackSignal.js';

type PromiseWithResolvers = ReturnType<typeof Promise.withResolvers<void>>;
type PendingRequestsRecord<T> = Readonly<{
  waiter: PromiseWithResolvers;
  callback(value: T): T;
  canceled: boolean;
}>;

export interface OperationSubscription {
  cancel(): void;
}

/**
 * A full-stack signal that holds an arbitrary value.
 */
export class ValueSignal<T> extends FullStackSignal<T> {
  readonly #pendingRequests = new Map<string, PendingRequestsRecord<T>>();

  set(value: T): void {
    this.value = value;
  }

  replace(expected: T, newValue: T): void {
    this[$update](createReplaceStateEvent(expected, newValue));
  }

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
