import { Command, type SignalCommand } from './commands.js';
import { findInsertIndex } from './ListPosition.js';
import { createId, InconsistentTreeError } from './utils.js';

export type Node = Readonly<{
  value: unknown;
  listChildren: readonly number[];
  mapChildren: Readonly<Record<string, number>>;
  lastUpdate?: number;
  parent: number | null;
}>;

export const BASE_NODE: Node = {
  value: null,
  listChildren: [],
  mapChildren: {},
  parent: null,
};

export type NodeTree = ReadonlyMap<number, Node>;

export function apply(tree: NodeTree, nodeId: number, command: SignalCommand): NodeTree | null {
  const lastUpdate = { lastUpdate: command.id };

  if (command instanceof Command.TRANSACTION) {
    let result: NodeTree | null = tree;

    for (const cmd of command.commands) {
      result = apply(result, nodeId, cmd);

      if (!result) {
        return null;
      }
    }

    return result;
  }

  const node = tree.get(nodeId);

  if (!node) {
    return null;
  }

  if (command instanceof Command.VALUE_CONDITION) {
    return node.value !== command.expectedValue ? null : tree;
  }

  if (command instanceof Command.POSITION_CONDITION) {
    const position = findInsertIndex(node.listChildren, command.position);
    return position === -1 || node.listChildren[position] !== command.childId ? null : tree;
  }

  if (command instanceof Command.KEY_CONDITION) {
    return node.mapChildren[command.key] !== command.expectedChildId ? null : tree;
  }

  if (command instanceof Command.LAST_UPDATE_CONDITION) {
    return node.lastUpdate !== command.expectedLastUpdate ? null : tree;
  }

  if (command instanceof Command.ADOPT_AS) {
    return !tree.has(command.childId) || command.key in node.mapChildren
      ? null
      : new Map(tree).set(nodeId, {
          ...node,
          mapChildren: { ...node.mapChildren, [command.key]: command.childId },
          ...lastUpdate,
        });
  }

  if (command instanceof Command.ADOPT_AT) {
    if (!tree.has(command.childId) || node.listChildren.some((childId) => childId === command.childId)) {
      return null;
    }

    const index = findInsertIndex(node.listChildren, command.position);

    return index === -1
      ? null
      : new Map(tree).set(nodeId, {
          ...node,
          listChildren: [...node.listChildren.slice(0, index), command.childId, ...node.listChildren.slice(index)],
          ...lastUpdate,
        });
  }

  if (command instanceof Command.INCREMENT) {
    return (typeof node.value === 'number' && typeof command.delta === 'number') ||
      (typeof node.value === 'bigint' && typeof command.delta === 'bigint')
      ? new Map(tree).set(nodeId, {
          ...node,
          // @ts-expect-error: too generic for TS
          value: node.value + command.delta, // eslint-disable-line @typescript-eslint/restrict-plus-operands
          ...lastUpdate,
        })
      : null;
  }

  if (command instanceof Command.CLEAR) {
    return new Map(tree).set(nodeId, {
      ...BASE_NODE,
      parent: node.parent,
      ...lastUpdate,
    });
  }

  if (command instanceof Command.REMOVE_BY_KEY) {
    if (!(command.key in node.mapChildren)) {
      return null;
    }

    const { [command.key]: _, ...rest } = node.mapChildren;

    return new Map(tree).set(nodeId, {
      ...node,
      mapChildren: rest,
      ...lastUpdate,
    });
  }

  if (command instanceof Command.PUT || command instanceof Command.PUT_IF_ABSENT) {
    if (command.key in node.mapChildren && command instanceof Command.PUT_IF_ABSENT) {
      return null;
    }

    const nodeToPutId = createId();

    const nodeToPut: Node = {
      ...BASE_NODE,
      value: command.value,
      parent: nodeId,
      ...lastUpdate,
    };

    return new Map(tree).set(nodeToPutId, nodeToPut).set(nodeId, {
      ...node,
      mapChildren: {
        ...node.mapChildren,
        [command.key]: nodeToPutId,
      },
      ...lastUpdate,
    });
  }

  if (command instanceof Command.INSERT) {
    const index = findInsertIndex(node.listChildren, command.position);

    const nodeToInsertId = createId();

    const nodeToInsert: Node = {
      ...BASE_NODE,
      value: command.value,
      parent: nodeId,
      ...lastUpdate,
    };

    return index === -1
      ? null
      : new Map(tree).set(nodeToInsertId, nodeToInsert).set(nodeId, {
          ...node,
          listChildren: [...node.listChildren.slice(0, index), nodeToInsertId, ...node.listChildren.slice(index)],
          ...lastUpdate,
        });
  }

  if (command instanceof Command.SET) {
    return new Map(tree).set(nodeId, {
      ...node,
      value: command.value,
      ...lastUpdate,
    });
  }

  if (command instanceof Command.REMOVE) {
    if ((command.expectedParentId && command.expectedParentId !== node.parent) || node.parent === null) {
      return null;
    }

    const newTree = new Map(tree);
    newTree.delete(nodeId);

    const parent = newTree.get(node.parent);

    if (!parent) {
      return null;
    }

    const key = Object.entries(parent.mapChildren).find(([, childId]) => childId === nodeId)?.[0];

    if (key) {
      const { [key]: _, ...rest } = parent.mapChildren;

      return newTree.set(node.parent, {
        ...parent,
        mapChildren: rest,
        ...lastUpdate,
      });
    }

    return newTree.set(node.parent, {
      ...parent,
      listChildren: parent.listChildren.filter((childId) => childId !== nodeId),
      ...lastUpdate,
    });
  }

  return null;
}

export function getMapValue(
  tree: NodeTree,
  mapChildren: Readonly<Record<string, number>>,
): Readonly<Record<string, unknown>> {
  return Object.fromEntries(
    Object.entries(mapChildren).map(([key, id]) => {
      if (!tree.has(id)) {
        throw new InconsistentTreeError(id);
      }

      return [key, tree.get(id)!.value] as const;
    }),
  );
}

export function getListValue(tree: NodeTree, listChildren: readonly number[]): readonly unknown[] {
  return listChildren.map((id) => {
    if (!tree.has(id)) {
      throw new InconsistentTreeError(id);
    }

    return tree.get(id)!.value;
  });
}
