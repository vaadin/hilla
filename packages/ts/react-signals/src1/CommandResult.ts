import type { Node } from './NodeTree.js';

export type CommandResultNodeModification = Readonly<{
  oldNode: Node | null;
  newNode: Node | null;
}>;

export type CommandResult = Readonly<{
  accepted: boolean;
  updates: Readonly<Record<number, CommandResultNodeModification | undefined>>;
}>;
