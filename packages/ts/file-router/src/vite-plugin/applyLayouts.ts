import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import type { RouteMeta } from './collectRoutesFromFS.js';

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
  return {
    ...route,
    flowLayout: true,
  };
}

/**
 * Check if there is a layout available that can handle the given path.
 * Layouts match the starting parts of the path so '/' will match all paths
 * and '/home' matches '/home' anything with the start path '/home/*'
 *
 * @param layoutPaths - available layout paths
 * @param path - to check for layout
 */
function layoutExists(layoutPaths: string[], path: string) {
  const splitPath: string[] = path.split('/');

  return layoutPaths.some((layout) => {
    if (layout.length === 0) {
      return true;
    }
    const splitLayout: string[] = layout.split('/');
    if (splitLayout.length > splitPath.length) {
      return false;
    }
    for (let i = 0; i < splitLayout.length; i++) {
      if (splitPath[i] !== splitLayout[i]) {
        return false;
      }
    }
    return true;
  });
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
    const layoutPaths = availableLayouts.map((layout) => stripLeadingSlash(layout.path));

    return routeMeta.map((route) =>
      layoutExists(layoutPaths, stripLeadingSlash(route.path)) ? enableFlowLayout(route) : route,
    );
  } catch (e: unknown) {
    return routeMeta;
  }
}
