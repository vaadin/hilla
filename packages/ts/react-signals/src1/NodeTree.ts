import { Command, type SignalCommand } from './commands.js';
import { findInsertIndex } from './ListPosition.js';
import { createId, ZERO_ID } from './utils.js';

export type Node = Readonly<{
  id: number;
  value: unknown;
  listChildren: readonly number[];
  mapChildren: Readonly<Record<string, number>>;
  lastAppliedCommand?: number;
  parent: number | null;
}>;

export const ZERO_NODE: Node = {
  id: ZERO_ID,
  value: null,
  listChildren: [],
  mapChildren: {},
  parent: null,
};

export type NodeTree = ReadonlyMap<number, Node>;

export function apply(tree: NodeTree, nodeId: number, command: SignalCommand): NodeTree | null {
  const lastAppliedCommand = { lastAppliedCommand: command.id };

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
    return node.lastAppliedCommand !== command.expectedLastUpdate ? null : tree;
  }

  if (command instanceof Command.ADOPT_AS) {
    return !tree.has(command.childId) || command.key in node.mapChildren
      ? null
      : new Map(tree).set(node.id, {
          ...node,
          mapChildren: { ...node.mapChildren, [command.key]: command.childId },
          ...lastAppliedCommand,
        });
  }

  if (command instanceof Command.ADOPT_AT) {
    if (!tree.has(command.childId) || node.listChildren.some((childId) => childId === command.childId)) {
      return null;
    }

    const index = findInsertIndex(node.listChildren, command.position);

    return index === -1
      ? null
      : new Map(tree).set(node.id, {
          ...node,
          listChildren: [...node.listChildren.slice(0, index), command.childId, ...node.listChildren.slice(index)],
          ...lastAppliedCommand,
        });
  }

  if (command instanceof Command.INCREMENT) {
    return (typeof node.value === 'number' && typeof command.delta === 'number') ||
      (typeof node.value === 'bigint' && typeof command.delta === 'bigint')
      ? new Map(tree).set(node.id, {
          ...node,
          // @ts-expect-error: too generic for TS
          value: node.value + command.delta, // eslint-disable-line @typescript-eslint/restrict-plus-operands
          ...lastAppliedCommand,
        })
      : null;
  }

  if (command instanceof Command.CLEAR) {
    return new Map(tree).set(node.id, {
      ...ZERO_NODE,
      id: node.id,
      parent: node.parent,
      ...lastAppliedCommand,
    });
  }

  if (command instanceof Command.REMOVE_BY_KEY) {
    if (!(command.key in node.mapChildren)) {
      return null;
    }

    const { [command.key]: _, ...rest } = node.mapChildren;

    return new Map(tree).set(node.id, {
      ...node,
      mapChildren: rest,
      ...lastAppliedCommand,
    });
  }

  if (command instanceof Command.PUT || command instanceof Command.PUT_IF_ABSENT) {
    if (command.key in node.mapChildren && command instanceof Command.PUT_IF_ABSENT) {
      return null;
    }

    const nodeToPut: Node = {
      ...ZERO_NODE,
      id: createId(),
      value: command.value,
      parent: node.id,
      ...lastAppliedCommand,
    };

    return new Map(tree).set(nodeToPut.id, nodeToPut).set(node.id, {
      ...node,
      mapChildren: {
        ...node.mapChildren,
        [command.key]: nodeToPut.id,
      },
      ...lastAppliedCommand,
    });
  }

  if (command instanceof Command.INSERT) {
    const index = findInsertIndex(node.listChildren, command.position);

    const nodeToInsert: Node = {
      ...ZERO_NODE,
      id: createId(),
      value: command.value,
      parent: node.id,
      ...lastAppliedCommand,
    };

    return index === -1
      ? null
      : new Map(tree).set(nodeToInsert.id, nodeToInsert).set(node.id, {
          ...node,
          listChildren: [...node.listChildren.slice(0, index), nodeToInsert.id, ...node.listChildren.slice(index)],
          ...lastAppliedCommand,
        });
  }

  if (command instanceof Command.SET) {
    return new Map(tree).set(node.id, {
      ...node,
      value: command.value,
      ...lastAppliedCommand,
    });
  }

  if (command instanceof Command.REMOVE) {
    if ((command.expectedParentId && command.expectedParentId !== node.parent) || node.parent === null) {
      return null;
    }

    const newTree = new Map(tree);
    newTree.delete(node.id);

    const parent = newTree.get(node.parent);

    if (!parent) {
      return null;
    }

    const key = Object.entries(parent.mapChildren).find(([, childId]) => childId === node.id)?.[0];

    if (key) {
      const { [key]: _, ...rest } = parent.mapChildren;

      return newTree.set(parent.id, {
        ...parent,
        mapChildren: rest,
        ...lastAppliedCommand,
      });
    }

    return newTree.set(parent.id, {
      ...parent,
      listChildren: parent.listChildren.filter((childId) => childId !== node.id),
      ...lastAppliedCommand,
    });
  }

  return null;
}
