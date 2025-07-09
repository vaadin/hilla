import {
  createInsertCommand,
  createRemoveCommand,
  isAdoptAtCommand,
  isInsertCommand,
  isPositionCondition,
  isRemoveCommand,
  ListPosition,
  ZERO,
  type AdoptAtCommand,
  type InsertCommand,
  type PositionCondition,
  type RemoveCommand,
} from './commands.js';
import {
  $createOperation,
  $processServerResponse,
  $resolveOperation,
  $update,
  FullStackSignal,
  type Operation,
  type ServerConnectionConfig,
} from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a list of values. Supports atomic updates to the list structure.
 * Each value in the list is accessed as a separate ValueSignal instance.
 */
export class ListSignal<T> extends FullStackSignal<Array<ValueSignal<T>>> {
  constructor(config: ServerConnectionConfig, id?: string) {
    super([], config, id);
  }

  /**
   * Inserts a value as the first entry in this list.
   * @param value - The value to insert
   * @returns An operation containing a signal for the inserted entry and the eventual result
   */
  insertFirst(value: T): Operation {
    return this.insertAt(value, ListPosition.first());
  }

  /**
   * Inserts a value as the last entry in this list.
   * @param value - The value to insert
   * @returns An operation containing a signal for the inserted entry and the eventual result
   */
  insertLast(value: T): Operation {
    return this.insertAt(value, ListPosition.last());
  }

  /**
   * Inserts a value at the given position in this list.
   * @param value - The value to insert
   * @param at - The insert position
   * @returns An operation containing a signal for the inserted entry and the eventual result
   */
  insertAt(value: T, at: ListPosition): Operation {
    const command = createInsertCommand<T>(ZERO, value, at);
    const promise = this[$update](command);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  /**
   * Removes the given child from this list.
   * @param child - The child to remove
   * @returns An operation containing the eventual result
   */
  remove(child: ValueSignal<T>): Operation {
    const command = createRemoveCommand(ZERO, child.id);
    const promise = this[$update](command);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  protected override [$processServerResponse](
    command: InsertCommand<T> | RemoveCommand | AdoptAtCommand | PositionCondition,
  ): void {
    if (isInsertCommand<T>(command)) {
      const valueSignal = new ValueSignal<T>(command.value, this.server.config, command.targetNodeId);
      let insertIndex = this.value.length;
      const pos = command.position;
      if (pos.after === '' && pos.before == null) {
        insertIndex = 0;
      } else if (pos.after == null && pos.before === '') {
        insertIndex = this.value.length;
      } else if (typeof pos.after === 'string' && pos.after !== '') {
        const idx = this.value.findIndex((v) => v.id === pos.after);
        insertIndex = idx !== -1 ? idx + 1 : this.value.length;
      } else if (typeof pos.before === 'string' && pos.before !== '') {
        const idx = this.value.findIndex((v) => v.id === pos.before);
        insertIndex = idx !== -1 ? idx : this.value.length;
      }
      const newList = [...this.value.slice(0, insertIndex), valueSignal, ...this.value.slice(insertIndex)];
      this.value = newList;
      this[$resolveOperation](command.commandId, undefined);
    } else if (isRemoveCommand(command)) {
      // TODO: Update local state for removal
      this[$resolveOperation](command.commandId, undefined);
    } else if (isAdoptAtCommand(command)) {
      // TODO: Update local state for move
      this[$resolveOperation](command.commandId, undefined);
    } else if (isPositionCondition(command)) {
      // TODO: Handle position verification
      this[$resolveOperation](command.commandId, undefined);
    }
  }
}
