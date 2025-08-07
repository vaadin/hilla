import { createIncrementCommand, isIncrementCommand, type SignalCommand } from './commands.js';
import {
  $createOperation,
  $processServerResponse,
  $resolveOperation,
  $setValueQuietly,
  $update,
  type Operation,
} from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a numeric value. The value is updated as a single atomic change.
 */
export class NumberSignal extends ValueSignal<number> {
  /**
   * Atomically increments the value of this signal by the given delta amount.
   * The value is decremented if the delta is negative.
   * @param delta - The increment amount
   * @returns An operation containing the eventual result
   */
  incrementBy(delta: number): Operation {
    // If delta is zero, no need to send command to server
    if (delta === 0) {
      const resolvedPromise = Promise.resolve(undefined);
      return this[$createOperation]({ id: '', promise: resolvedPromise });
    }

    const command = createIncrementCommand('', delta);
    const promise = this[$update](command);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  /**
   * Gets the value of this signal as an integer.
   */
  valueAsInt(): number {
    return Math.trunc(this.value);
  }

  protected override [$processServerResponse](command: SignalCommand): void {
    if (isIncrementCommand(command)) {
      this[$setValueQuietly](this.value + command.delta);
      this[$resolveOperation](command.commandId, undefined);
    } else {
      super[$processServerResponse](command);
    }
  }
}
