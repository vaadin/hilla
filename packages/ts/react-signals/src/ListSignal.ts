import {
  createInsertCommand,
  createRemoveCommand,
  type InsertCommand,
  type RemoveCommand,
  type AdoptAtCommand,
  type PositionCondition,
  type ListPosition,
  isInsertCommand,
  isRemoveCommand,
  isAdoptAtCommand,
  isPositionCondition,
} from './commands.js';
import {
  $createOperation,
  $processServerResponse,
  $resolveOperation,
  $update,
  type Operation,
  FullStackSignal,
} from './FullStackSignal.js';
import type { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a list of values. Supports atomic updates to the list structure.
 * Each value in the list is accessed as a separate ValueSignal instance.
 */
export class ListSignal<T> extends FullStackSignal<Array<ValueSignal<T>>> {
  constructor(config: ConstructorParameters<typeof FullStackSignal>[1], id?: string) {
    super([], config, id);
  }

  /**
   * Inserts a value as the first entry in this list.
   * @param value - The value to insert
   * @returns An operation containing a signal for the inserted entry and the eventual result
   */
  insertFirst(value: T): Operation {
    return this.insertAt(value, { after: null, before: null }); // TODO: ListPosition.first()
  }

  /**
   * Inserts a value as the last entry in this list.
   * @param value - The value to insert
   * @returns An operation containing a signal for the inserted entry and the eventual result
   */
  insertLast(value: T): Operation {
    return this.insertAt(value, { after: null, before: null }); // TODO: ListPosition.last()
  }

  /**
   * Inserts a value at the given position in this list.
   * @param value - The value to insert
   * @param at - The insert position
   * @returns An operation containing a signal for the inserted entry and the eventual result
   */
  insertAt(value: T, at: ListPosition): Operation {
    const command = createInsertCommand<T>(this.id, value, at);
    const promise = this[$update](command);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  /**
   * Removes the given child from this list.
   * @param child - The child to remove
   * @returns An operation containing the eventual result
   */
  remove(child: ValueSignal<T>): Operation {
    const command = createRemoveCommand(this.id, child.id);
    const promise = this[$update](command);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  protected override [$processServerResponse](
    command: InsertCommand<T> | RemoveCommand | AdoptAtCommand | PositionCondition,
  ): void {
    if (isInsertCommand<T>(command)) {
      // TODO: Update local state with inserted value
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
