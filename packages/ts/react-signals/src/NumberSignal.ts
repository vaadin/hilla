import { createIncrementStateEvent, isIncrementStateEvent, type StateEvent } from './events.js';
import { $processServerResponse, $setValueQuietly, $update } from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

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
 * const counter = CounterService.counter();
 *
 * return (
 *    <Button onClick={() => counter.incrementBy(1)}>
 *      Click count: { counter }
 *    </Button>
 *    <Button onClick={() => counter.value = 0}>Reset</Button>
 * );
 * ```
 */
export class NumberSignal extends ValueSignal<number> {
  readonly #sentIncrementEvents = new Map<string, StateEvent>();
  /**
   * Increments the value by the specified delta. The delta can be negative to
   * decrease the value.
   *
   * This method differs from using the `++` or `+=` operators directly on the
   * signal value. It performs an atomic operation to prevent conflicts from
   * concurrent changes, ensuring that other users' modifications are not
   * accidentally overwritten.
   *
   * @param delta - The delta to increment the value by. The delta can be
   * negative.
   */
  incrementBy(delta: number): void {
    if (delta === 0) {
      return;
    }
    this[$setValueQuietly](this.value + delta);
    const event = createIncrementStateEvent(delta);
    this.#sentIncrementEvents.set(event.id, event);
    this[$update](event);
  }

  protected override [$processServerResponse](event: StateEvent): void {
    if (event.accepted && isIncrementStateEvent(event)) {
      if (this.#sentIncrementEvents.has(event.id)) {
        this.#sentIncrementEvents.delete(event.id);
        return;
      }
      this[$setValueQuietly](this.value + event.value);
    } else {
      super[$processServerResponse](event);
    }
  }
}
