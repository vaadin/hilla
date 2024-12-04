export function transformTree<T extends readonly unknown[], U, C extends object | null = null>(
  nodes: T,
  context: C,
  transformer: (nodes: T, next: (nodes: T, ctx?: C) => U, context: C) => U,
): U {
  return transformer(nodes, (n, ctx = context) => transformTree(n, ctx, transformer), context);
}
