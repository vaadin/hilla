import {
  EDGE,
  isAdoptAtCommand,
  isClearCommand,
  isIncrementCommand,
  isInsertCommand,
  isPositionCondition,
  isPutCommand,
  isRemoveByKeyCommand,
  isRemoveCommand,
  isSetCommand,
  isSnapshotCommand,
  isTransactionCommand,
  isValueCondition,
  type Id,
  type ListPosition,
  type Node,
  type SignalCommand,
} from './commands.js';

/**
 * The id of a node in a signal tree. The root node always uses the zero id.
 */
export type NodeId = Id;

/**
 * The id of the root node of a signal tree.
 */
export const ROOT: NodeId = '';

/**
 * The data of a single node in a signal tree.
 */
export type NodeData = Readonly<{
  value?: unknown;
  listChildren: readonly NodeId[];
  mapChildren: Readonly<Record<string, NodeId>>;
  parent: NodeId | null;
}>;

/**
 * An immutable snapshot of a signal tree.
 * <p>
 * Applying a command never mutates a tree but returns a new one, which makes it
 * possible to keep a confirmed tree around while a queue of unconfirmed
 * commands is repeatedly replayed on top of it.
 */
export type NodeTree = ReadonlyMap<NodeId, NodeData>;

const EMPTY_NODE: NodeData = {
  value: undefined,
  listChildren: [],
  mapChildren: {},
  parent: null,
};

/**
 * Creates a tree that only contains a root node with the given value.
 */
export function emptyTree(rootValue?: unknown): NodeTree {
  return new Map([[ROOT, { ...EMPTY_NODE, value: rootValue }]]);
}

/**
 * Converts the nodes of a snapshot command into a tree.
 */
export function snapshotToTree(nodes: Record<NodeId, Node>): NodeTree {
  const tree = new Map<NodeId, NodeData>();

  for (const [id, node] of Object.entries<Partial<Node>>(nodes)) {
    // Alias nodes have no children of their own. They are only created by
    // commands that this client never sends, so they are simply ignored.
    if (!node.listChildren) {
      continue;
    }

    tree.set(id, {
      value: node.value,
      listChildren: [...node.listChildren],
      mapChildren: { ...node.mapChildren },
      parent: node.parent ?? null,
    });
  }

  return tree;
}

/**
 * Gets the value of the given node, or `undefined` if there is no such node.
 */
export function getNodeValue(tree: NodeTree, nodeId: NodeId): unknown {
  return tree.get(nodeId)?.value;
}

/**
 * Gets the ids of the list children of the given node.
 */
export function getListChildren(tree: NodeTree, nodeId: NodeId): readonly NodeId[] {
  return tree.get(nodeId)?.listChildren ?? [];
}

/**
 * Gets the map children of the given node, keyed by the map key.
 */
export function getMapChildren(tree: NodeTree, nodeId: NodeId): Readonly<Record<string, NodeId>> {
  return tree.get(nodeId)?.mapChildren ?? {};
}

/**
 * Compares two values the same way as the server does, i.e. based on JSON
 * equality rather than on identity.
 */
function jsonEquals(a: unknown, b: unknown): boolean {
  if (Object.is(a, b)) {
    return true;
  }

  if (typeof a !== 'object' || typeof b !== 'object' || a === null || b === null) {
    return false;
  }

  if (Array.isArray(a) || Array.isArray(b)) {
    return (
      Array.isArray(a) && Array.isArray(b) && a.length === b.length && a.every((item, i) => jsonEquals(item, b[i]))
    );
  }

  const entries = Object.entries(a);
  return (
    entries.length === Object.keys(b).length &&
    entries.every(([key, value]) => key in b && jsonEquals(value, (b as Record<string, unknown>)[key]))
  );
}

/**
 * Finds the index at which a child should be inserted into the given list of
 * children, or -1 if the position cannot be satisfied.
 * <p>
 * This mirrors `MutableTreeRevision#findInsertIndex` on the server so that an
 * optimistically applied insert ends up in the same place as the confirmed one.
 */
export function findInsertIndex(children: readonly NodeId[], position: ListPosition): number {
  const { after, before } = position;

  if (after != null) {
    let index: number;

    if (after === EDGE) {
      // After the edge means insert first
      index = 0;
    } else {
      const indexOf = children.indexOf(after);
      if (indexOf === -1) {
        return -1;
      }
      index = indexOf + 1;
    }

    if (before != null) {
      // Both ends are defined, so the other end has to match as well
      const atPosition = index < children.length ? children[index] : EDGE;
      if (atPosition !== before) {
        return -1;
      }
    }

    return index;
  }

  if (before == null) {
    // Neither end defined is not a valid position
    return -1;
  }

  if (before === EDGE) {
    // Before the edge means insert last
    return children.length;
  }

  return children.indexOf(before);
}

function removeSubtree(nodes: Map<NodeId, NodeData>, nodeId: NodeId): void {
  const node = nodes.get(nodeId);
  if (!node) {
    return;
  }

  nodes.delete(nodeId);

  for (const childId of node.listChildren) {
    removeSubtree(nodes, childId);
  }
  for (const childId of Object.values(node.mapChildren)) {
    removeSubtree(nodes, childId);
  }
}

/**
 * Removes the given node from the children of its parent, leaving the node
 * itself and its own children in place.
 */
function unlink(nodes: Map<NodeId, NodeData>, nodeId: NodeId): void {
  // The root node id is an empty string, so the parent has to be compared
  // against null explicitly
  const node = nodes.get(nodeId);
  if (node?.parent == null) {
    return;
  }

  const parent = nodes.get(node.parent);
  if (!parent) {
    return;
  }

  const mapKey = Object.entries(parent.mapChildren).find(([, childId]) => childId === nodeId)?.[0];

  if (mapKey !== undefined) {
    const { [mapKey]: _detached, ...mapChildren } = parent.mapChildren;
    nodes.set(node.parent, { ...parent, mapChildren });
  } else {
    nodes.set(node.parent, {
      ...parent,
      listChildren: parent.listChildren.filter((childId) => childId !== nodeId),
    });
  }
}

function detach(nodes: Map<NodeId, NodeData>, nodeId: NodeId): void {
  unlink(nodes, nodeId);
  removeSubtree(nodes, nodeId);
}

function withNode(tree: NodeTree, nodeId: NodeId, node: NodeData): NodeTree {
  return new Map(tree).set(nodeId, node);
}

/**
 * Applies a command to a tree and returns the resulting tree, or `null` if the
 * command is not applicable to the tree.
 * <p>
 * The logic here mirrors `MutableTreeRevision` on the server. Most importantly,
 * a node created by a command gets the id of the command that created it, which
 * is what makes an optimistically created node converge with the confirmed one
 * and makes it possible to target the new node before the server has confirmed
 * its creation.
 */
export function applyCommand(tree: NodeTree, command: SignalCommand): NodeTree | null {
  if (isTransactionCommand(command)) {
    // A transaction is applied atomically: either all of it or none of it
    let result: NodeTree = tree;

    for (const nested of command.commands) {
      const next = applyCommand(result, nested);
      if (!next) {
        return null;
      }
      result = next;
    }

    return result;
  }

  if (isSnapshotCommand(command)) {
    return snapshotToTree(command.nodes);
  }

  const nodeId = command.targetNodeId;
  const node = tree.get(nodeId);
  if (!node) {
    return null;
  }

  if (isValueCondition(command)) {
    return jsonEquals(node.value, command.expectedValue) ? tree : null;
  }

  if (isPositionCondition(command)) {
    const index = findInsertIndex(node.listChildren, command.expectedPosition);
    return index !== -1 && node.listChildren[index] === command.childId ? tree : null;
  }

  if (isSetCommand(command)) {
    return withNode(tree, nodeId, { ...node, value: command.value });
  }

  if (isIncrementCommand(command)) {
    // An absent value is treated as zero, just like on the server
    const current = node.value ?? 0;
    if (typeof current !== 'number') {
      return null;
    }
    return withNode(tree, nodeId, { ...node, value: current + command.delta });
  }

  if (isClearCommand(command)) {
    const children = [...node.listChildren, ...Object.values(node.mapChildren)];
    if (children.length === 0) {
      return tree;
    }

    const nodes = new Map(tree);
    for (const childId of children) {
      removeSubtree(nodes, childId);
    }
    // Clearing only detaches the children, the value of the node is kept
    nodes.set(nodeId, { ...node, listChildren: [], mapChildren: {} });

    return nodes;
  }

  if (isInsertCommand(command)) {
    const childId = command.commandId;
    if (tree.has(childId)) {
      return null;
    }

    const index = findInsertIndex(node.listChildren, command.position);
    if (index === -1) {
      return null;
    }

    const nodes = new Map(tree);
    nodes.set(childId, { ...EMPTY_NODE, value: command.value, parent: nodeId });
    nodes.set(nodeId, {
      ...node,
      listChildren: [...node.listChildren.slice(0, index), childId, ...node.listChildren.slice(index)],
    });

    return nodes;
  }

  if (isPutCommand(command)) {
    if (command.key in node.mapChildren) {
      // Putting to an existing key updates the value of the existing child
      // instead of creating a new node, which keeps the child signal stable
      const existingId = node.mapChildren[command.key];
      const existing = tree.get(existingId);
      return existing ? withNode(tree, existingId, { ...existing, value: command.value }) : null;
    }

    const childId = command.commandId;
    if (tree.has(childId)) {
      return null;
    }

    const nodes = new Map(tree);
    nodes.set(childId, { ...EMPTY_NODE, value: command.value, parent: nodeId });
    nodes.set(nodeId, { ...node, mapChildren: { ...node.mapChildren, [command.key]: childId } });

    return nodes;
  }

  if (isRemoveByKeyCommand(command)) {
    if (!(command.key in node.mapChildren)) {
      return null;
    }

    const nodes = new Map(tree);
    detach(nodes, node.mapChildren[command.key]);

    return nodes;
  }

  if (isRemoveCommand(command)) {
    if (node.parent === null) {
      return null;
    }
    if (node.parent !== command.expectedParentId) {
      return null;
    }

    const nodes = new Map(tree);
    detach(nodes, nodeId);

    return nodes;
  }

  if (isAdoptAtCommand(command)) {
    const { childId } = command;
    if (!tree.has(childId)) {
      return null;
    }

    const nodes = new Map(tree);
    // The child is moved rather than removed, so its own children are kept
    unlink(nodes, childId);

    const parent = nodes.get(nodeId);
    const child = tree.get(childId);
    if (!parent || !child) {
      return null;
    }

    const index = findInsertIndex(parent.listChildren, command.position);
    if (index === -1) {
      return null;
    }

    nodes.set(childId, { ...child, parent: nodeId });
    nodes.set(nodeId, {
      ...parent,
      listChildren: [...parent.listChildren.slice(0, index), childId, ...parent.listChildren.slice(index)],
    });

    return nodes;
  }

  return null;
}
