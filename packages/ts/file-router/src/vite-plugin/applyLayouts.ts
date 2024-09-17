import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import type { RouteMeta } from './collectRoutesFromFS';

/**
 * The information about a particular server-side layout.
 */
export type LayoutMeta = Readonly<{
  path: string;
}>;

function stripLeadingSlash(path: string) {
  return path.startsWith('/') ? path.slice(1) : path;
}

function enableFlowLayout(route: RouteMeta): RouteMeta {
  const routeWithFlowLayout = {
    ...route,
    flowLayout: true,
  };
  return route.children
    ? {
        ...routeWithFlowLayout,
        children: route.children.map(enableFlowLayout),
      }
    : routeWithFlowLayout;
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
  if (!existsSync(layoutsFile)) {
    return routeMeta;
  }
  const layoutContents = await readFile(layoutsFile, 'utf-8');
  const availableLayouts: readonly LayoutMeta[] = JSON.parse(layoutContents);
  const layoutPaths = new Set(availableLayouts.map((layout) => stripLeadingSlash(layout.path)));

  return routeMeta.map((route) => (layoutPaths.has(stripLeadingSlash(route.path)) ? enableFlowLayout(route) : route));
}
