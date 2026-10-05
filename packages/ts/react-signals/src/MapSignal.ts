import { ChildSignals } from './ChildSignals.js';
import { createClearCommand, createPutCommand, createRemoveByKeyCommand } from './commands.js';
import type { ServerConnectionConfig } from './Connection.js';
import { FullStackSignal } from './FullStackSignal.js';
import { getMapChildren, type NodeTree } from './NodeTree.js';
import type { InsertOperation, Operation } from './Operation.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a map from string keys to values. Supports atomic updates
 * to the map structure. Each value in the map is accessed as a separate
 * {@link ValueSignal} instance that stays valid for as long as the entry is
 * part of the map.
 */
export class MapSignal<T> extends FullStackSignal<ReadonlyMap<string, ValueSignal<T>>> {
  readonly #children = new ChildSignals((id) => new ValueSignal<T>(undefined, this.tree, id));

  // The constructor of a full-stack signal is protected, so it has to be
  // redeclared to define what a map signal is created from
  // eslint-disable-next-line @typescript-eslint/no-useless-constructor
  constructor(config: ServerConnectionConfig) {
    super(config);
  }

  override get value(): ReadonlyMap<string, ValueSignal<T>> {
    return super.value;
  }

  /**
   * @readonly
   */
  override set value(_: never) {
    throw new Error('The value of a map signal cannot be set. Use put, remove or clear instead.');
  }

  /**
   * Sets the value for the given key, replacing the previous value if the key
   * is already present.
   * <p>
   * Note that putting to a key that is already present updates the existing
   * entry, which means that the signal of that entry stays the same.
   *
   * @param key - The key to set the value for
   * @param value - The value to set
   * @returns An operation containing the signal of the entry
   */
  put(key: string, value: T): InsertOperation<ValueSignal<T>> {
    const command = createPutCommand(this.id, key, value);
    // Putting to an existing key updates that node instead of creating a new
    // one, so the entry has to be looked up before the command is applied
    const children = getMapChildren(this.tree.nodes.peek(), this.id);
    const nodeId = key in children ? children[key] : command.commandId;

    return { ...this.tree.submit(command), signal: this.#children.get(nodeId) };
  }

  /**
   * Removes the entry with the given key.
   *
   * @param key - The key to remove
   * @returns An operation that allows reacting to the outcome
   */
  remove(key: string): Operation {
    return this.submit(createRemoveByKeyCommand(this.id, key));
  }

  /**
   * Removes all entries from this map.
   *
   * @returns An operation that allows reacting to the outcome
   */
  clear(): Operation {
    return this.submit(createClearCommand(this.id));
  }

  protected override deriveValue(nodes: NodeTree): ReadonlyMap<string, ValueSignal<T>> {
    const entries = Object.entries(getMapChildren(nodes, this.id)).filter(([, childId]) => nodes.has(childId));
    this.#children.retain(entries.map(([, childId]) => childId));

    return new Map(entries.map(([key, childId]) => [key, this.#children.get(childId)]));
  }
}
