import type { SignalCommand } from './commands.js';
import type { ServerConnectionConfig } from './Connection.js';
import { computed, Signal, type ReadonlySignal } from './core.js';
import { ROOT, type NodeId, type NodeTree } from './NodeTree.js';
import type { Operation } from './Operation.js';
import { SignalTree } from './SignalTree.js';
import { randomId } from './utils.js';

export type { ServerConnectionConfig } from './Connection.js';
export type { InsertOperation, Operation } from './Operation.js';

/**
 * A signal that is backed by a signal on the server. Each change to the value
 * is sent to the server and applied locally right away, and it is reverted
 * again if the server rejects it. At the same time, each change made by any
 * other client is applied to the local signal and propagated to its
 * subscribers.
 * <p>
 * A full-stack signal is a view into a node of a {@link SignalTree}. A signal
 * that represents a collection shares its tree with the signals of its
 * children, which means that a child signal is a first-class signal: it stays
 * up to date, it can be subscribed to, and it can be used in a computed signal.
 *
 * @internal
 */
export abstract class FullStackSignal<T> extends Signal<T> {
  /**
   * The id of the tree node that this signal represents. The signal of the
   * value provided by the endpoint is the root node of the tree.
   */
  readonly id: NodeId;

  /**
   * The tree that holds the state shared by this signal and its children.
   */
  readonly tree: SignalTree;

  /**
   * Defines whether the signal is currently awaiting a server-side response.
   */
  readonly pending: ReadonlySignal<boolean>;

  /**
   * Defines whether the signal has an error.
   */
  readonly error: ReadonlySignal<Error | undefined>;

  readonly #derived: ReadonlySignal<T>;

  protected constructor(source: ServerConnectionConfig | SignalTree, id: NodeId = ROOT, defaultValue?: unknown) {
    super(undefined);

    this.tree = source instanceof SignalTree ? source : new SignalTree(randomId(), source, defaultValue);
    this.id = id;
    this.pending = this.tree.pending;
    this.error = this.tree.error;
    this.#derived = computed(() => this.deriveValue(this.tree.nodes.value));
  }

  override get value(): T {
    return this.#derived.value;
  }

  override set value(_: T) {
    throw new Error('The value of this signal cannot be set directly.');
  }

  override peek(): T {
    return this.#derived.peek();
  }

  /**
   * Derives the value of this signal from the current state of the tree. Each
   * type of signal defines what part of the tree it represents.
   *
   * @param nodes - The current state of the tree
   */
  protected abstract deriveValue(nodes: NodeTree): T;

  /**
   * Submits a command to the server, applying it optimistically until the
   * server confirms or rejects it.
   *
   * @param command - The command to submit
   * @returns An operation that allows reacting to the outcome
   */
  protected submit(command: SignalCommand): Operation {
    return this.tree.submit(command);
  }

  /**
   * Creates an operation for a change that doesn't need to be sent to the
   * server at all.
   */
  // eslint-disable-next-line @typescript-eslint/class-methods-use-this
  protected noopOperation(): Operation {
    return { result: Promise.resolve() };
  }
}
