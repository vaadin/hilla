/**
 * Traverse a tree and yield the sequence of parent nodes to each leaf node.
 *
 * @param tree - The tree to traverse.
 * @param parents - The sequence of parent nodes for the current node.
 */
export default function* traverse<T extends { children?: readonly T[] }>(
  tree: T,
  parents: readonly T[] = [],
): Generator<readonly T[], undefined, undefined> {
  const chain = [...parents, tree];
  const children = tree.children ?? [];

  if (children.length === 0) {
    yield chain;
  }

  for (const child of children) {
    yield* traverse(child, chain);
  }
}
