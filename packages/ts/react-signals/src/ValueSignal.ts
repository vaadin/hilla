import { createSetCommand } from './commands.js';
import type { ServerConnectionConfig } from './Connection.js';
import { FullStackSignal } from './FullStackSignal.js';
import { getNodeValue, type NodeId, type NodeTree } from './NodeTree.js';
import type { Operation } from './Operation.js';
import type { SignalTree } from './SignalTree.js';

/**
 * A full-stack signal that holds an arbitrary value.
 */
export class ValueSignal<T> extends FullStackSignal<T> {
  readonly #defaultValue: T | undefined;

  /**
   * Creates a signal for the value provided by an endpoint method.
   *
   * @param defaultValue - The value to use until the server has sent the
   * current value
   * @param config - The description of the endpoint method that provides the
   * signal
   */
  constructor(defaultValue: T | undefined, config: ServerConnectionConfig);
  /**
   * Creates a signal for a node of an existing tree. Used to represent the
   * children of a collection signal.
   *
   * @internal
   */
  constructor(defaultValue: T | undefined, tree: SignalTree, id: NodeId);
  constructor(defaultValue: T | undefined, source: ServerConnectionConfig | SignalTree, id?: NodeId) {
    super(source, id, defaultValue);
    this.#defaultValue = defaultValue;
  }

  override get value(): T {
    return super.value;
  }

  override set value(value: T) {
    this.set(value);
  }

  /**
   * Sets the value.
   * <p>
   * Note that the command that is sent to the server as the result of this
   * operation is not taking the last seen value into account and will overwrite
   * the shared value on the server unconditionally (AKA: "Last Write Wins").
   * <p>
   * The new value is visible locally right away and reverts to the previous
   * value if the server rejects the change.
   *
   * @param value - The new value
   * @returns An operation that allows reacting to the outcome
   */
  set(value: T): Operation {
    return this.submit(createSetCommand(this.id, value));
  }

  protected override deriveValue(nodes: NodeTree): T {
    return (getNodeValue(nodes, this.id) ?? this.#defaultValue) as T;
  }
}
