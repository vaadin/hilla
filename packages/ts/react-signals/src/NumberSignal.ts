import { createIncrementCommand } from './commands.js';
import type { Operation } from './Operation.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a numeric value. The value is updated as a single atomic
 * change.
 */
export class NumberSignal extends ValueSignal<number> {
  /**
   * Atomically increments the value of this signal by the given delta amount.
   * The value is decremented if the delta is negative.
   * <p>
   * The increment is applied locally right away and it is reverted again if the
   * server rejects it. Unlike {@link ValueSignal.set}, an increment is applied
   * relative to the value on the server, so concurrent increments from several
   * clients all take effect.
   *
   * @param delta - The increment amount
   * @returns An operation that allows reacting to the outcome
   */
  incrementBy(delta: number): Operation {
    if (delta === 0) {
      return this.noopOperation();
    }

    return this.submit(createIncrementCommand(this.id, delta));
  }

  /**
   * Gets the value of this signal as an integer.
   */
  valueAsInt(): number {
    return Math.trunc(this.value);
  }
}
