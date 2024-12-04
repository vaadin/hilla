import { readFile } from 'node:fs/promises';
import { join } from 'node:path/posix';
import { transformTree } from '../shared/transformTree.js';
import type { RouteMeta } from './collectRoutesFromFS.js';

/**
 * The information about a particular server-side layout.
 */
export type LayoutMeta = Readonly<{
  path: string;
}>;

function strip(path: string) {
  return path.replace(/^\/*(.+)\/*$/u, '$1');
}

/**
 * Enables Flow layout flag on the matching routes based on the information from the layouts JSON file.
 *
 * @param routeMeta - The routes tree to process.
 * @param layoutsFile - The server layouts JSON file.
 * @returns Processed routes tree.
 */
export default async function applyLayouts(
  routeMeta: readonly RouteMeta[],
  layoutsFile: URL,
): Promise<readonly RouteMeta[]> {
  try {
    const layoutContents = await readFile(layoutsFile, 'utf-8');
    const availableLayouts: readonly LayoutMeta[] = JSON.parse(layoutContents);
    const layoutPaths = availableLayouts.map((layout) => strip(layout.path));

    return transformTree<readonly RouteMeta[], readonly RouteMeta[], { path: string }>(
      routeMeta,
      { path: '' },
      (metas, next, ctx) =>
        metas.map((meta) => {
          const currentPath = join(ctx.path, strip(meta.path));
          const children = meta.children ? next(meta.children, { path: currentPath }) : undefined;

          return layoutPaths.some((path) => currentPath.includes(path))
            ? { ...meta, flowLayout: true, children }
            : { ...meta, children };
        }),
    );
  } catch (e: unknown) {
    if (e instanceof Error && 'code' in e && e.code === 'ENOENT') {
      return routeMeta;
    }

    throw e;
  }
}
