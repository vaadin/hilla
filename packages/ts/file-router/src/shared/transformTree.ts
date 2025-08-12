export type TreeTransformerNextCallback<T extends readonly unknown[], U, C extends object | null = null> = (
  nodes: T,
  context?: C,
) => U;

export type TreeTransformer<T extends readonly unknown[], U, C extends object | null = null> = (
  nodes: T,
  next: TreeTransformerNextCallback<T, U, C>,
  context: C,
) => U;

export function transformTree<T extends readonly unknown[], U, C extends object | null = null>(
  nodes: T,
  context: C,
  transformer: TreeTransformer<T, U, C>,
): U {
  return transformer(nodes, (n, ctx = context) => transformTree(n, ctx, transformer), context);
}
