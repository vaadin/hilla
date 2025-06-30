import { createSetCommand, isSetCommand, type SignalCommand } from './commands.js';
import {
  $createOperation,
  $processServerResponse,
  $resolveOperation,
  $setValueQuietly,
  $update,
  FullStackSignal,
  type Operation,
} from './FullStackSignal.js';

type PendingRequestsRecord<T> = Readonly<{
  id: string;
  callback(value: T): T;
}> & { canceled: boolean };

/**
 * An operation subscription that can be canceled.
 */
export interface OperationSubscription extends Operation {
  cancel(): void;
}

/**
 * A full-stack signal that holds an arbitrary value.
 */
export class ValueSignal<T> extends FullStackSignal<T> {
  readonly #pendingRequests = new Map<string, PendingRequestsRecord<T>>();

  /**
   * Sets the value.
   * Note that the value change event that is propagated to the server as the
   * result of this operation is not taking the last seen value into account and
   * will overwrite the shared value on the server unconditionally (AKA: "Last
   * Write Wins"). If you need to perform a conditional update, use the
   * `replace` method instead.
   *
   * @param value - The new value.
   * @returns An operation object that allows to perform additional actions.
   */
  set(value: T): Operation {
    const command = createSetCommand('', value);
    const promise = this[$update](command);
    this[$setValueQuietly](value);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  protected override [$processServerResponse](command: SignalCommand): void {
    const record = this.#pendingRequests.get(command.commandId);
    if (record) {
      this.#pendingRequests.delete(command.commandId);
    }

    this.#recalculateState(command);

    // `then` callbacks can be associated to the record or the event
    // it depends on the operation that was performed
    [record?.id, command.commandId].filter(Boolean).forEach((id) => this[$resolveOperation](id!, undefined));
  }

  #recalculateState(command: SignalCommand): void {
    if (isSetCommand<T>(command)) {
      this.value = command.value;
    }
  }
}
