import { createSetCommand } from './commands.js';
import type { ServerConnectionConfig } from './Connection.js';
import { FullStackSignal, type Operation } from './FullStackSignal.js';
import { getNodeValue, type NodeTree } from './NodeTree.js';

/**
 * A full-stack signal that holds an arbitrary value.
 */
export class ValueSignal<T> extends FullStackSignal<T> {
  readonly #defaultValue: T | undefined;

  constructor(value: T | undefined, config: ServerConnectionConfig, id?: string, parent?: FullStackSignal<any>) {
    super(value, config, id, parent);
    this.#defaultValue = value;
  }

  protected deriveValue(tree: NodeTree): T {
    // Root signals use '' (the tree root); child signals also use '' since
    // each child has its own tree with the default value at the root.
    return (getNodeValue(tree, '') ?? this.#defaultValue) as T;
  }

  override get value(): T {
    return super.value;
  }

  override set value(v: T) {
    this.set(v);
  }

  /**
   * Sets the value.
   * Note that the value change event that is propagated to the server as the
   * result of this operation is not taking the last seen value into account and
   * will overwrite the shared value on the server unconditionally (AKA: "Last
   * Write Wins").
   *
   * @param value - The new value.
   * @returns An operation object that allows to perform additional actions.
   */
  set(value: T): Operation {
    return this.sendCommand(createSetCommand('', value));
  }
}
