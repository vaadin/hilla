import { createReplaceStateEvent, type StateEvent } from './events.js';
import { $processServerResponse, $update, FullStackSignal } from './FullStackSignal.js';

type PromiseWithResolvers = ReturnType<typeof Promise.withResolvers<void>>;
type PendingApprovalsRecord<T> = Readonly<{
  waiter: PromiseWithResolvers;
  callback(value: T): T;
}>;

/**
 * A full-stack signal that holds an arbitrary value.
 */
export class ValueSignal<T> extends FullStackSignal<T> {
  readonly #pendingApprovals = new Map<string, PendingApprovalsRecord<T>>();

  set(value: T): void {
    this.value = value;
  }

  replace(expected: T, newValue: T): void {
    this[$update](createReplaceStateEvent(expected, newValue));
  }

  async update(callback: (value: T) => T): Promise<void> {
    const newValue = callback(this.value);
    const event = createReplaceStateEvent(this.value, newValue);
    this[$update](event);
    const waiter = Promise.withResolvers<void>();
    this.#pendingApprovals.set(event.id, { callback, waiter });
    return await waiter.promise;
  }

  protected override [$processServerResponse](event: StateEvent<T>): void {
    const record = this.#pendingApprovals.get(event.id);

    if (event.type === 'reject' && record) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.update(record.callback);
    }

    if (event.type === 'snapshot') {
      if (record) {
        record.waiter.resolve();
        this.#pendingApprovals.delete(event.id);
      }

      this.value = event.value;
    }
  }
}
