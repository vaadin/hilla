import { createIncrementCommand, isIncrementCommand, type SignalCommand } from './commands.js';
import {
  $createOperation,
  $processServerResponse,
  $resolveOperation,
  $update,
  type Operation,
} from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a numeric value. The value is updated as a single atomic change.
 */
export class NumberSignal extends ValueSignal<number> {
  // Track commands that have already been processed to avoid double-increment
  readonly #processedCommands = new Set<string>();

  /**
   * Atomically increments the value of this signal by the given delta amount.
   * The value is decremented if the delta is negative.
   * @param delta - The increment amount
   * @returns An operation containing the eventual result
   */
  incrementBy(delta: number): Operation {
    const command = createIncrementCommand('', delta);
    this.#processedCommands.add(command.commandId);
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
      if (!this.#processedCommands.has(command.commandId)) {
        this.value += command.value;
      }
      this.#processedCommands.delete(command.commandId);
      this[$resolveOperation](command.commandId, undefined);
    } else {
      super[$processServerResponse](command);
    }
  }
}
