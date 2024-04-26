export function transformTree<T extends readonly unknown[], U>(
  nodes: T,
  transformer: (nodes: T, next: (...nodes: T) => U) => U,
): U {
  return transformer(nodes, (...n) => transformTree(n, transformer));
}
