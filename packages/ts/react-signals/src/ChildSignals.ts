import type { NodeId } from './NodeTree.js';

/**
 * A cache of the child signals of a collection signal.
 * <p>
 * A collection signal derives its value from the tree every time the tree
 * changes. Without a cache, each derivation would create a new signal instance
 * for each child, which would make it impossible to hold on to a child signal:
 * the instance in hand would immediately be replaced by another one. The cache
 * makes sure that the same node is always represented by the same signal
 * instance for as long as the node is part of the tree.
 *
 * @internal
 */
export class ChildSignals<S> {
  readonly #signals = new Map<NodeId, S>();
  readonly #create: (id: NodeId) => S;

  constructor(create: (id: NodeId) => S) {
    this.#create = create;
  }

  /**
   * Gets the signal for the given node, creating it if it doesn't exist yet.
   */
  get(id: NodeId): S {
    let child = this.#signals.get(id);

    if (!child) {
      child = this.#create(id);
      this.#signals.set(id, child);
    }

    return child;
  }

  /**
   * Discards the signals of all nodes that are not in the given collection of
   * node ids.
   */
  retain(ids: Iterable<NodeId>): void {
    const retained = new Set(ids);

    for (const id of this.#signals.keys()) {
      if (!retained.has(id)) {
        this.#signals.delete(id);
      }
    }
  }
}
