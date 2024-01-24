import type { RouteMeta } from './collectRoutes.js';

function* traverse(
  views: RouteMeta,
  parents: readonly string[] = [],
): Generator<readonly string[], undefined, undefined> {
  const chain = [...parents, views.path];

  if (views.children.length === 0) {
    yield chain;
  }

  for (const child of views.children) {
    yield* traverse(child, chain);
  }
}

export default function generateJson(views: RouteMeta): string {
  const paths: string[] = [];

  for (const branch of traverse(views)) {
    const path = branch.join('/');
    paths.push(path ? path : '/');
  }

  return JSON.stringify(paths, null, 2);
}
