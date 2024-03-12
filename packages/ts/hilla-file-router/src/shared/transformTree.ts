/**
 * Transforms the whole route tree into a new format.
 *
 * @param node - The route to transform.
 * @param getChildren - A function that returns the children of the route.
 * @param transformer - A function that transforms the route and its children.
 *
 * @returns The transformed route.
 */
export function transformTreeSync<T, U>(
  node: T,
  getChildren: (node: T) => IterableIterator<T> | null | undefined,
  transformer: (node: T, children: readonly U[]) => U,
): U {
  const children = getChildren(node);

  return transformer(
    node,
    children ? Array.from(children, (child) => transformTreeSync(child, getChildren, transformer)) : [],
  );
}

export async function transformTree<T, U>(
  node: T,
  getChildren: (node: T) => IterableIterator<T> | null | undefined,
  transformer: (node: T, children: readonly U[]) => Promise<U>,
): Promise<U> {
  const children = getChildren(node);

  return transformer(
    node,
    children
      ? await Promise.all(Array.from(children, async (child) => transformTree(child, getChildren, transformer)))
      : [],
  );
}
