import {
  createIncrementCommand,
  isIncrementCommand,
  isSetCommand,
  isSnapshotCommand,
  type SignalCommand,
} from './commands.js';
import {
  $createOperation,
  $handleRejection,
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
   * Pending increment operations applied optimistically, keyed by commandId.
   */
  readonly #pendingIncrements = new Map<string, number>();

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
      const resolvedPromise = Promise.resolve(undefined);
      return this[$createOperation]({ id: '', promise: resolvedPromise });
    }

    const command = createIncrementCommand('', delta);
    const promise = this[$update](command);
    // Apply optimistically
    this.#pendingIncrements.set(command.commandId, delta);
    this[$setValueQuietly](this.value + delta);
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
      if (this.#pendingIncrements.has(command.commandId)) {
        // Our own increment — already applied optimistically, just remove from pending
        this.#pendingIncrements.delete(command.commandId);
      } else {
        // Remote increment — apply the delta
        this[$setValueQuietly](this.value + command.delta);
      }
      this[$resolveOperation](command.commandId, undefined);
    } else {
      // A set or snapshot resets the confirmed state — clear pending increments
      if (isSetCommand(command) || isSnapshotCommand(command)) {
        this.#pendingIncrements.clear();
      }
      super[$processServerResponse](command);
    }
  }

  protected override [$handleRejection](command: SignalCommand): void {
    if (isIncrementCommand(command)) {
      const delta = this.#pendingIncrements.get(command.commandId);
      if (delta !== undefined) {
        // Revert the optimistic increment
        this[$setValueQuietly](this.value - delta);
        this.#pendingIncrements.delete(command.commandId);
      }
    }
    super[$handleRejection](command);
  }
}
