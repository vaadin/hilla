import { nanoid } from 'nanoid';
import { Signal } from './core.js';
import { type StateEvent, StateEventType } from './types';

declare module '@preact/signals-core' {
  // https://github.com/preactjs/signals/issues/351#issuecomment-1515488634
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  // eslint-disable-next-line @typescript-eslint/no-shadow
  class Signal {
    protected S(node: any): void;
    protected U(node: any): void;
  }
}

class DependencyTrackSignal<T> extends Signal<T> {
  readonly #onSubscribe: () => void;
  readonly #onUnsubscribe: () => void;

  #subscribeCount = 0;

  constructor(value: T | undefined, onSubscribe: () => void, onUnsubscribe: () => void) {
    super(value);
    this.#onSubscribe = onSubscribe;
    this.#onUnsubscribe = onUnsubscribe;
  }

  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  protected override S(node: any): void {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    // eslint-disable-next-line @typescript-eslint/no-unsafe-call
    super.S(node);
    if (this.#subscribeCount === 0) {
      this.#onSubscribe.call(null);
    }
    this.#subscribeCount += 1;
  }

  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  protected override U(node: any): void {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    // eslint-disable-next-line @typescript-eslint/no-unsafe-call
    super.U(node);
    this.#subscribeCount -= 1;
    if (this.#subscribeCount === 0) {
      this.#onUnsubscribe.call(null);
    }
  }
}

// eslint-disable-next-line import/no-mutable-exports
export let setInternalValue: <T>(signal: ValueSignal<T>, value: T) => void;

/**
 * A signal that holds a value. The underlying
 * value of this signal is stored and updated as a
 * shared value on the server.
 *
 * @internal
 */
export abstract class ValueSignal<T> extends DependencyTrackSignal<T> {
  static {
    setInternalValue = (signal: ValueSignal<unknown>, value: unknown): void => signal.#setInternalValue(value);
  }

  readonly #publish: (event: StateEvent) => Promise<boolean>;

  /**
   * Creates a new ValueSignal instance.
   * @param publish - The function that publishes the
   * value of the signal to the server.
   * @param value - The initial value of the signal
   * @param onSubscribe - The function that is called
   * when the signal is subscribed to.
   * @param onUnsubscribe - The function that is called
   * when the signal is unsubscribed from.
   * @defaultValue undefined
   */
  constructor(
    publish: (event: StateEvent) => Promise<boolean>,
    value: T | undefined,
    onSubscribe: () => void,
    onUnsubscribe: () => void,
  ) {
    super(value, onSubscribe, onUnsubscribe);
    this.#publish = publish;
  }

  /**
   * Returns the value of the signal.
   */
  override get value(): T {
    return super.value;
  }

  /**
   * Publishes the new value to the server.
   * Note that this method is not setting
   * the signal's value.
   *
   * @param value - The new value of the signal
   * to be published to the server.
   */
  override set value(value: T) {
    const id = nanoid();
    // set the local value to be used for latency compensation and offline support:
    this.#setInternalValue(value);
    // publish the update to the server:
    this.#publish({ id, type: StateEventType.SET, value }).catch((error) => {
      throw error;
    });
  }

  /**
   * Sets the value of the signal.
   * @param value - The new value of the signal.
   * @internal
   */
  #setInternalValue(value: T): void {
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
 * ```tsx
 * const counter = CounterService.counter();
 *
 * return (
 *   <>
 *     <Button onClick={() => counter++)}>
 *       Click count: { counter }
 *     </Button>
 *     <Button onClick={() => counter.value = 0}>Reset</Button>
 *   </>
 * );
 * ```
 */
export class NumberSignal extends ValueSignal<number> {}
