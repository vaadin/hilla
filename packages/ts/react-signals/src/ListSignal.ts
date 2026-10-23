import { ChildSignals } from './ChildSignals.js';
import { createClearCommand, createInsertCommand, createRemoveCommand, ListPosition } from './commands.js';
import type { ServerConnectionConfig } from './Connection.js';
import { FullStackSignal } from './FullStackSignal.js';
import { getListChildren, type NodeTree } from './NodeTree.js';
import type { InsertOperation, Operation } from './Operation.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a list of values. Supports atomic updates to the list
 * structure. Each value in the list is accessed as a separate
 * {@link ValueSignal} instance that stays valid for as long as the entry is
 * part of the list.
 */
export class ListSignal<T> extends FullStackSignal<ReadonlyArray<ValueSignal<T>>> {
  readonly #children = new ChildSignals((id) => new ValueSignal<T>(undefined, this.tree, id));

  // The constructor of a full-stack signal is protected, so it has to be
  // redeclared to define what a list signal is created from
  // eslint-disable-next-line @typescript-eslint/no-useless-constructor
  constructor(config: ServerConnectionConfig) {
    super(config);
  }

  override get value(): ReadonlyArray<ValueSignal<T>> {
    return super.value;
  }

  /**
   * @readonly
   */
  override set value(_: never) {
    throw new Error('The value of a list signal cannot be set. Use insert, remove or clear instead.');
  }

  /**
   * Inserts a value as the first entry in this list.
   *
   * @param value - The value to insert
   * @returns An operation containing the signal of the inserted entry
   */
  insertFirst(value: T): InsertOperation<ValueSignal<T>> {
    return this.insertAt(value, ListPosition.first());
  }

  /**
   * Inserts a value as the last entry in this list.
   *
   * @param value - The value to insert
   * @returns An operation containing the signal of the inserted entry
   */
  insertLast(value: T): InsertOperation<ValueSignal<T>> {
    return this.insertAt(value, ListPosition.last());
  }

  /**
   * Inserts a value at the given position in this list.
   * <p>
   * The entry is visible locally right away and it is removed again if the
   * server rejects the insert. The signal of the new entry can be used
   * immediately since the id of the new node is derived from the id of the
   * command that creates it, in the same way as on the server.
   *
   * @param value - The value to insert
   * @param at - The insert position
   * @returns An operation containing the signal of the inserted entry
   */
  insertAt(value: T, at: ListPosition): InsertOperation<ValueSignal<T>> {
    const command = createInsertCommand(this.id, value, at);

    return { ...this.tree.submit(command), signal: this.#children.get(command.commandId) };
  }

  /**
   * Removes the given entry from this list.
   *
   * @param child - The entry to remove
   * @returns An operation that allows reacting to the outcome
   */
  remove(child: ValueSignal<T>): Operation {
    return this.submit(createRemoveCommand(child.id, this.id));
  }

  /**
   * Removes all entries from this list.
   *
   * @returns An operation that allows reacting to the outcome
   */
  clear(): Operation {
    return this.submit(createClearCommand(this.id));
  }

  protected override deriveValue(nodes: NodeTree): ReadonlyArray<ValueSignal<T>> {
    // A snapshot may refer to a node that it doesn't describe, in which case
    // there is nothing to show for that entry
    const childIds = getListChildren(nodes, this.id).filter((childId) => nodes.has(childId));
    this.#children.retain(childIds);

    return childIds.map((childId) => this.#children.get(childId));
  }
}
