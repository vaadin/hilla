/**
 * Transforms the whole route tree into a new format.
 *
 * @param route - The route to transform.
 * @param getChildren - A function that returns the children of the route.
 * @param transformer - A function that transforms the route and its children.
 *
 * @returns The transformed route.
 */
export function transformRoute<T, U>(
  route: T,
  getChildren: (route: T) => IterableIterator<T> | null | undefined,
  transformer: (route: T, children: readonly U[]) => U,
): U {
  const children = getChildren(route);

  return transformer(
    route,
    children ? Array.from(children, (child) => transformRoute(child, getChildren, transformer)) : [],
  );
}
