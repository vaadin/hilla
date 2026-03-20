import { createIncrementCommand } from './commands.js';
import type { Operation } from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a numeric value. The value is updated as a single atomic change.
 */
export class NumberSignal extends ValueSignal<number> {
  /**
   * Atomically increments the value of this signal by the given delta amount.
   * The value is decremented if the delta is negative. The increment is applied
   * optimistically — the local value updates immediately before server
   * confirmation.
   * @param delta - The increment amount
   * @returns An operation containing the eventual result
   */
  incrementBy(delta: number): Operation {
    if (delta === 0) {
      return this.createResolvedOperation();
    }

    return this.sendCommand(createIncrementCommand('', delta));
  }

  /**
   * Gets the value of this signal as an integer.
   */
  valueAsInt(): number {
    return Math.trunc(this.value);
  }
}
