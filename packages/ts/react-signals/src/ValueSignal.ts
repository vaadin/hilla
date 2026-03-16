import { createSetCommand, isSetCommand, isSnapshotCommand, type Node, type SignalCommand } from './commands.js';
import {
  $createOperation,
  $handleRejection,
  $processServerResponse,
  $resolveOperation,
  $setValueQuietly,
  $update,
  FullStackSignal,
  type Operation,
  type ServerConnectionConfig,
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
   * The last server-confirmed value. Used to revert optimistic updates on
   * rejection.
   */
  #confirmedValue: T | undefined;

  /**
   * Command IDs of pending set operations that have been applied
   * optimistically but not yet confirmed by the server.
   */
  readonly #pendingSetIds = new Set<string>();

  constructor(value: T | undefined, config: ServerConnectionConfig, id?: string, parent?: FullStackSignal<any>) {
    super(value, config, id, parent);
    this.#confirmedValue = value;
  }

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
    this.#pendingSetIds.add(command.commandId);
    this[$setValueQuietly](value);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  protected override [$processServerResponse](command: SignalCommand): void {
    const record = this.#pendingRequests.get(command.commandId);
    if (record) {
      this.#pendingRequests.delete(command.commandId);
    }

    if (isSetCommand<T>(command)) {
      this.#confirmedValue = command.value;
      if (this.#pendingSetIds.delete(command.commandId)) {
        // Our own set — already applied optimistically. Only update display
        // if there are no other pending sets (to preserve newer optimistic value).
        if (this.#pendingSetIds.size === 0) {
          this[$setValueQuietly](command.value);
        }
      } else if (this.#pendingSetIds.size === 0) {
        // Foreign set — update display only if no pending local sets
        this[$setValueQuietly](command.value);
      }
    } else if (isSnapshotCommand(command)) {
      const node = command.nodes[''] as Node | undefined;
      if (node && 'value' in node) {
        this.#confirmedValue = node.value as T;
        this[$setValueQuietly](node.value as T);
      }
      this.#pendingSetIds.clear();
    }

    // `then` callbacks can be associated to the record or the event
    // it depends on the operation that was performed
    [record?.id, command.commandId].filter(Boolean).forEach((id) => this[$resolveOperation](id!, undefined));
  }

  protected override [$handleRejection](command: SignalCommand): void {
    if (isSetCommand(command)) {
      this.#pendingSetIds.delete(command.commandId);
      // Revert to confirmed value if no other pending sets
      if (this.#pendingSetIds.size === 0) {
        this[$setValueQuietly](this.#confirmedValue as T);
      }
    }
    super[$handleRejection](command);
  }
}
