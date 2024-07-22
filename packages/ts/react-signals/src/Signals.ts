import { Signal } from './core.js';
import type { SetEvent, StateEvent } from './types';

/**
 * A signal that holds a value. The underlying
 * value of this signal is stored and updated as a
 * shared value on the server.
 *
 * @internal
 */
export abstract class ValueSignal<T> extends Signal<T> {
  readonly #publish: (event: StateEvent) => Promise<boolean>;

  /**
   * Creates a new ValueSignal instance.
   * @param publish The function that publishes the
   * value of the signal to the server.
   * @param value The initial value of the signal
   * @defaultValue undefined
   */
  constructor(publish: (event: StateEvent) => Promise<boolean>, value?: T) {
    super(value);
    this.#publish = publish;
  }

  /**
   * Returns the value of the signal.
   */
  override get value() {
    return super.value;
  }

  /**
   * Publishes the new value to the server.
   * Note that this method is not setting
   * the signal's value.
   *
   * @param value The new value of the signal
   * to be published to the server.
   */
  override set value(value: T) {
    const id = crypto.randomUUID();
    this.#publish({ id, type: 'set', value } as SetEvent).then(r => undefined);
  }

  /**
   * Sets the value of the signal.
   * @param value The new value of the signal.
   * @internal
   */
  setValue(value: T): void {
    super.value = value;
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
 * const counter = CounterService.counter();
 *
 * return (
 *    <Button onClick={() => counter++)}>
 *      Click count: { counter }
 *    </Button>
 *    <Button onClick={() => counter.value = 0}>Reset</Button>
 * );
 */
export class NumberSignal extends ValueSignal<number> {}
