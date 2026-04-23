import { createClearCommand, createInsertCommand, createRemoveCommand, ListPosition, ZERO } from './commands.js';
import type { ServerConnectionConfig } from './Connection.js';
import { FullStackSignal, type Operation } from './FullStackSignal.js';
import { getListChildren, type NodeTree } from './NodeTree.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a list of values. Supports atomic updates to the list structure.
 * Each value in the list is accessed as a separate ValueSignal instance.
 */
export class ListSignal<T> extends FullStackSignal<Array<ValueSignal<T>>> {
  constructor(config: ServerConnectionConfig, id?: string) {
    super([], config, id);
  }

  protected deriveValue(tree: NodeTree): Array<ValueSignal<T>> {
    const childIds = getListChildren(tree, '');
    return childIds
      .map((childId) => {
        const childNode = tree.get(childId);
        if (childNode?.value !== undefined) {
          return new ValueSignal(childNode.value as T, this.getConfig(), childId, this);
        }
        return null;
      })
      .filter(Boolean) as Array<ValueSignal<T>>;
  }

  override get value(): Array<ValueSignal<T>> {
    return super.value;
  }

  /**
   * @readonly
   */
  override set value(_: never) {
    throw new Error('Value of the collection signals cannot be set.');
  }

  /**
   * Inserts a value as the first entry in this list.
   * @param value - The value to insert
   * @returns An operation containing the eventual result
   */
  insertFirst(value: T): Operation {
    return this.insertAt(value, ListPosition.first());
  }

  /**
   * Inserts a value as the last entry in this list.
   * @param value - The value to insert
   * @returns An operation containing the eventual result
   */
  insertLast(value: T): Operation {
    return this.insertAt(value, ListPosition.last());
  }

  /**
   * Inserts a value at the given position in this list.
   * @param value - The value to insert
   * @param at - The insert position
   * @returns An operation containing the eventual result
   */
  insertAt(value: T, at: ListPosition): Operation {
    return this.sendCommand(createInsertCommand(ZERO, value, at));
  }

  /**
   * Removes the given child from this list.
   * @param child - The child to remove
   * @returns An operation containing the eventual result
   */
  remove(child: ValueSignal<T>): Operation {
    return this.sendCommand(createRemoveCommand(child.id, ZERO));
  }

  /**
   * Removes all children from this list.
   * @returns An operation containing the eventual result
   */
  clear(): Operation {
    return this.sendCommand(createClearCommand(ZERO));
  }
}
