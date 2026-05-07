import {
  type ListPosition,
  type SignalCommand,
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
} from './commands.js';
import { randomId } from './utils.js';

export type NodeId = string;

export type NodeData = Readonly<{
  value?: unknown;
  listChildren: readonly NodeId[];
  mapChildren: Readonly<Record<string, NodeId>>;
  parent: NodeId | null;
}>;

export type NodeTree = ReadonlyMap<NodeId, NodeData>;

const BASE_NODE: NodeData = {
  value: undefined,
  listChildren: [],
  mapChildren: {},
  parent: null,
};

/**
 * Creates an empty tree with a single root node.
 */
export function emptyTree(rootValue?: unknown): NodeTree {
  return new Map([['', { ...BASE_NODE, value: rootValue }]]);
}

/**
 * Computes the insertion index for a given ListPosition within a list of child IDs.
 */
function computeInsertIndex(children: readonly NodeId[], pos: ListPosition): number {
  if (pos.after === '' && pos.before == null) {
    // After edge -> insert first
    return 0;
  }
  if (pos.after == null && pos.before === '') {
    // Before edge -> insert last
    return children.length;
  }
  if (typeof pos.after === 'string' && pos.after !== '') {
    const idx = children.indexOf(pos.after);
    if (idx === -1) {
      return -1;
    }
    const position = idx + 1;
    // Validate before constraint if present
    if (pos.before != null) {
      const atPosition = position < children.length ? children[position] : '';
      if (atPosition !== pos.before) {
        return -1;
      }
    }
    return position;
  }
  if (typeof pos.before === 'string' && pos.before !== '') {
    return children.indexOf(pos.before);
  }
  return -1;
}

/**
 * Applies a command to a tree, returning a new tree or null if the command fails.
 */
export function applyCommand(tree: NodeTree, command: SignalCommand): NodeTree | null {
  const nodeId = command.targetNodeId;

  if (isTransactionCommand(command)) {
    let result: NodeTree | null = tree;
    for (const cmd of command.commands) {
      result = applyCommand(result, cmd);
      if (!result) {
        return null;
      }
    }
    return result;
  }

  if (isSnapshotCommand(command)) {
    const newTree = new Map<NodeId, NodeData>();
    for (const [id, node] of Object.entries(command.nodes)) {
      newTree.set(id, {
        value: node.value,
        listChildren: [...node.listChildren],
        mapChildren: { ...node.mapChildren },
        parent: node.parent,
      });
    }
    return newTree;
  }

  const node = tree.get(nodeId);
  if (!node) {
    return null;
  }

  if (isValueCondition(command)) {
    return node.value !== command.expectedValue ? null : tree;
  }

  if (isPositionCondition(command)) {
    const position = computeInsertIndex(node.listChildren, command.expectedPosition);
    return position === -1 || node.listChildren[position] !== command.childId ? null : tree;
  }

  if (isSetCommand(command)) {
    return new Map(tree).set(nodeId, {
      ...node,
      value: command.value,
    });
  }

  if (isIncrementCommand(command)) {
    // Treat undefined as 0 (default before server snapshot arrives)
    const current = node.value === undefined ? 0 : node.value;
    if (typeof current !== 'number') {
      return null;
    }
    return new Map(tree).set(nodeId, {
      ...node,
      value: current + command.delta,
    });
  }

  if (isClearCommand(command)) {
    return new Map(tree).set(nodeId, {
      ...BASE_NODE,
      parent: node.parent,
    });
  }

  if (isInsertCommand(command)) {
    const index = computeInsertIndex(node.listChildren, command.position);
    if (index === -1) {
      return null;
    }
    const newChildId = randomId();
    const newChild: NodeData = {
      ...BASE_NODE,
      value: command.value,
      parent: nodeId,
    };
    return new Map(tree).set(newChildId, newChild).set(nodeId, {
      ...node,
      listChildren: [...node.listChildren.slice(0, index), newChildId, ...node.listChildren.slice(index)],
    });
  }

  if (isPutCommand(command)) {
    const newChildId = randomId();
    const newChild: NodeData = {
      ...BASE_NODE,
      value: command.value,
      parent: nodeId,
    };
    return new Map(tree).set(newChildId, newChild).set(nodeId, {
      ...node,
      mapChildren: {
        ...node.mapChildren,
        [command.key]: newChildId,
      },
    });
  }

  if (isRemoveCommand(command)) {
    if (node.parent === null) {
      return null;
    }
    const newTree = new Map(tree);
    newTree.delete(nodeId);
    const parent = newTree.get(node.parent);
    if (!parent) {
      return null;
    }

    // Check if it's a map child
    const mapKey = Object.entries(parent.mapChildren).find(([, childId]) => childId === nodeId)?.[0];
    if (mapKey) {
      const { [mapKey]: _, ...rest } = parent.mapChildren;
      return newTree.set(node.parent, {
        ...parent,
        mapChildren: rest,
      });
    }

    // Otherwise it's a list child
    return newTree.set(node.parent, {
      ...parent,
      listChildren: parent.listChildren.filter((childId) => childId !== nodeId),
    });
  }

  if (isRemoveByKeyCommand(command)) {
    if (!(command.key in node.mapChildren)) {
      return null;
    }
    const { [command.key]: _, ...rest } = node.mapChildren;
    return new Map(tree).set(nodeId, {
      ...node,
      mapChildren: rest,
    });
  }

  if (isAdoptAtCommand(command)) {
    if (!tree.has(command.childId) || node.listChildren.includes(command.childId)) {
      // For adopt-at, first remove the child from current position, then re-insert
      if (node.listChildren.includes(command.childId)) {
        const withoutChild = node.listChildren.filter((id) => id !== command.childId);
        const index = computeInsertIndex(withoutChild, command.position);
        if (index === -1) {
          return null;
        }
        return new Map(tree).set(nodeId, {
          ...node,
          listChildren: [...withoutChild.slice(0, index), command.childId, ...withoutChild.slice(index)],
        });
      }
      return null;
    }
    const index = computeInsertIndex(node.listChildren, command.position);
    if (index === -1) {
      return null;
    }
    return new Map(tree).set(nodeId, {
      ...node,
      listChildren: [...node.listChildren.slice(0, index), command.childId, ...node.listChildren.slice(index)],
    });
  }

  return null;
}

/**
 * Gets the value of a node in the tree.
 */
export function getNodeValue(tree: NodeTree, nodeId: NodeId): unknown {
  return tree.get(nodeId)?.value;
}

/**
 * Gets the list children IDs of a node in the tree.
 */
export function getListChildren(tree: NodeTree, nodeId: NodeId): readonly NodeId[] {
  return tree.get(nodeId)?.listChildren ?? [];
}

/**
 * Gets the map children of a node as a Map.
 */
export function getMapChildren(tree: NodeTree, nodeId: NodeId): ReadonlyMap<string, NodeId> {
  const node = tree.get(nodeId);
  if (!node) {
    return new Map();
  }
  return new Map(Object.entries(node.mapChildren));
}
