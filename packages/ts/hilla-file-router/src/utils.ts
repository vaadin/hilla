export type AgnosticRoute<T, M extends object = Record<string, unknown>> = Readonly<{
  path: string;
  component?: T;
  meta?: M;
  children?: ReadonlyArray<AgnosticRoute<T>>;
}>;

export function processPattern(blank: string): string {
  return blank
    .replaceAll(/\[\.{3}.+\]/gu, '*')
    .replaceAll(/\[{2}(.+)\]{2}/gu, ':$1?')
    .replaceAll(/\[(.+)\]/gu, ':$1');
}

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
