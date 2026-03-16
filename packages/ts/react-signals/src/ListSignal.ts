import { CollectionSignal } from './CollectionSignal.js';
import {
  createClearCommand,
  createInsertCommand,
  createRemoveCommand,
  isAdoptAtCommand,
  isClearCommand,
  isInsertCommand,
  isPositionCondition,
  isRemoveCommand,
  isSetCommand,
  isSnapshotCommand,
  ListPosition,
  ZERO,
  type AdoptAtCommand,
  type ClearCommand,
  type InsertCommand,
  type PositionCondition,
  type RemoveCommand,
  type SetCommand,
  type SignalCommand,
  type SnapshotCommand,
} from './commands.js';
import {
  $createOperation,
  $handleRejection,
  $processServerResponse,
  $resolveOperation,
  $setValueQuietly,
  $update,
  type Operation,
  type ServerConnectionConfig,
} from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * Computes the insertion index for a given list position within a list of signals.
 */
function computeInsertIndex<T>(list: ReadonlyArray<ValueSignal<T>>, pos: ListPosition): number {
  if (pos.after === '' && pos.before == null) {
    return 0;
  }
  if (pos.after == null && pos.before === '') {
    return list.length;
  }
  if (typeof pos.after === 'string' && pos.after !== '') {
    const idx = list.findIndex((v) => v.id === pos.after);
    return idx !== -1 ? idx + 1 : list.length;
  }
  if (typeof pos.before === 'string' && pos.before !== '') {
    const idx = list.findIndex((v) => v.id === pos.before);
    return idx !== -1 ? idx : list.length;
  }
  return list.length;
}

/**
 * A signal containing a list of values. Supports atomic updates to the list structure.
 * Each value in the list is accessed as a separate ValueSignal instance.
 */
export class ListSignal<T> extends CollectionSignal<Array<ValueSignal<T>>> {
  /** Command IDs of pending insert operations applied optimistically. */
  readonly #pendingInserts = new Set<string>();

  /** Pending remove operations: commandId to child and index for potential revert. */
  readonly #pendingRemoves = new Map<string, { child: ValueSignal<T>; index: number }>();

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
   * Inserts a value at the given position in this list. The insert is applied
   * optimistically — the child appears immediately before server confirmation.
   * @param value - The value to insert
   * @param at - The insert position
   * @returns An operation containing a signal for the inserted entry and the eventual result
   */
  insertAt(value: T, at: ListPosition): Operation {
    const command = createInsertCommand(ZERO, value, at);
    const promise = this[$update](command);
    // Optimistic insert
    const valueSignal = new ValueSignal<T>(value, this.server.config, command.commandId, this);
    const insertIndex = computeInsertIndex(this.value, at);
    const newList = [...this.value.slice(0, insertIndex), valueSignal, ...this.value.slice(insertIndex)];
    this[$setValueQuietly](newList);
    this.#pendingInserts.add(command.commandId);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  /**
   * Removes the given child from this list. The removal is applied
   * optimistically — the child disappears immediately before server
   * confirmation.
   * @param child - The child to remove
   * @returns An operation containing the eventual result
   */
  remove(child: ValueSignal<T>): Operation {
    const command = createRemoveCommand(child.id, ZERO);
    const promise = this[$update](command);
    // Optimistic remove
    const removeIndex = this.value.findIndex((c) => c.id === child.id);
    if (removeIndex !== -1) {
      this.#pendingRemoves.set(command.commandId, { child, index: removeIndex });
      const newList = [...this.value.slice(0, removeIndex), ...this.value.slice(removeIndex + 1)];
      this[$setValueQuietly](newList);
    }
    return this[$createOperation]({ id: command.commandId, promise });
  }

  /**
   * Removes all children from this list.
   * @returns An operation containing the eventual result
   */
  clear(): Operation {
    const command = createClearCommand(ZERO);
    const promise = this[$update](command);
    this[$setValueQuietly]([]);
    this.#pendingInserts.clear();
    this.#pendingRemoves.clear();
    return this[$createOperation]({ id: command.commandId, promise });
  }

  protected override [$processServerResponse](
    command:
      | InsertCommand<T>
      | RemoveCommand
      | AdoptAtCommand
      | PositionCondition
      | SnapshotCommand
      | SetCommand<T>
      | ClearCommand,
  ): void {
    // Check if the command has a targetNodeId and reroute it to the corresponding child
    if ((isSnapshotCommand(command) || isSetCommand(command)) && command.targetNodeId) {
      const targetChild = this.value.find((child) => child.id === command.targetNodeId);

      if (targetChild) {
        targetChild[$processServerResponse](command);
        return;
      }
    }

    if (isInsertCommand<T>(command)) {
      if (this.#pendingInserts.has(command.commandId)) {
        // Our own insert — already applied optimistically
        this.#pendingInserts.delete(command.commandId);
      } else {
        // Remote insert
        const valueSignal = new ValueSignal<T>(command.value, this.server.config, command.commandId, this);
        const insertIndex = computeInsertIndex(this.value, command.position);
        const newList = [...this.value.slice(0, insertIndex), valueSignal, ...this.value.slice(insertIndex)];
        this[$setValueQuietly](newList);
      }
      this[$resolveOperation](command.commandId, undefined);
    } else if (isRemoveCommand(command)) {
      if (this.#pendingRemoves.has(command.commandId)) {
        // Our own remove — already applied optimistically
        this.#pendingRemoves.delete(command.commandId);
      } else {
        // Remote remove
        const removeIndex = this.value.findIndex((child) => child.id === command.targetNodeId);
        if (removeIndex !== -1) {
          const newList = [...this.value.slice(0, removeIndex), ...this.value.slice(removeIndex + 1)];
          this[$setValueQuietly](newList);
        }
      }
      this[$resolveOperation](command.commandId, undefined);
    } else if (isClearCommand(command)) {
      this[$setValueQuietly]([]);
      this.#pendingInserts.clear();
      this.#pendingRemoves.clear();
      this[$resolveOperation](command.commandId, undefined);
    } else if (isAdoptAtCommand(command)) {
      const moveIndex = this.value.findIndex((child) => child.id === command.childId);
      if (moveIndex !== -1) {
        const [movedChild] = this.value.splice(moveIndex, 1);
        const newIndex = computeInsertIndex(this.value, command.position);
        this.value.splice(newIndex, 0, movedChild);
      }
      this[$resolveOperation](command.commandId, undefined);
    } else if (isPositionCondition(command)) {
      this[$resolveOperation](command.commandId, undefined);
    } else if (isSnapshotCommand(command)) {
      const { nodes } = command;
      const listNode = nodes[''];

      const childrenIds = listNode.listChildren;
      const valueSignals = childrenIds
        .map((childId) => {
          const childNode = nodes[childId];
          if ('value' in childNode) {
            return new ValueSignal<T>(childNode.value as T, this.server.config, childId, this);
          }
          return null;
        })
        .filter(Boolean) as Array<ValueSignal<T>>;

      this[$setValueQuietly](valueSignals);
      this.#pendingInserts.clear();
      this.#pendingRemoves.clear();
      this[$resolveOperation](command.commandId, undefined);
    }
  }

  protected override [$handleRejection](command: SignalCommand): void {
    if (isInsertCommand(command) && this.#pendingInserts.has(command.commandId)) {
      // Revert optimistic insert
      const removeIndex = this.value.findIndex((child) => child.id === command.commandId);
      if (removeIndex !== -1) {
        const newList = [...this.value.slice(0, removeIndex), ...this.value.slice(removeIndex + 1)];
        this[$setValueQuietly](newList);
      }
      this.#pendingInserts.delete(command.commandId);
    } else if (isRemoveCommand(command)) {
      const removed = this.#pendingRemoves.get(command.commandId);
      if (removed) {
        // Revert optimistic remove — re-insert the child at its original position
        const insertIdx = Math.min(removed.index, this.value.length);
        const newList = [...this.value.slice(0, insertIdx), removed.child, ...this.value.slice(insertIdx)];
        this[$setValueQuietly](newList);
        this.#pendingRemoves.delete(command.commandId);
      }
    }
    super[$handleRejection](command);
  }
}
