import type { RouteMeta } from './collectRoutes.js';
import { processPattern, prepareConfig, type ViewConfig } from './utils.js';

function* traverse(
  views: RouteMeta,
  parents: readonly RouteMeta[] = [],
): Generator<readonly RouteMeta[], undefined, undefined> {
  const chain = [...parents, views];

  if (views.children.length === 0) {
    yield chain;
  }

  for (const child of views.children) {
    yield* traverse(child, chain);
  }
}

type RouteModule = Readonly<{
  default: unknown;
  config?: ViewConfig;
}>;

export default async function generateJson(views: RouteMeta): Promise<string> {
  const res = await Promise.all(
    Array.from(traverse(views), async (branch) => {
      const configs = await Promise.all(
        branch
          .filter(({ file, layout }) => !!file || !!layout)
          .map(({ file, layout }) => (file ? file : layout!).toString())
          .map(async (path) => {
            const { config, default: fn }: RouteModule = await import(`${path.substring(0, path.lastIndexOf('.'))}.js`);
            return prepareConfig(config, fn);
          }),
      );

      const key = branch.map(({ path }) => processPattern(path)).join('/');
      const value = configs[configs.length - 1];

      return [key, value] satisfies readonly [string, ViewConfig | undefined];
    }),
  );

  return JSON.stringify(Object.fromEntries(res));
}
