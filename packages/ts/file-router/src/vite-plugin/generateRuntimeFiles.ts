import { existsSync } from 'node:fs';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import type { Logger } from 'vite';
import collectRoutesFromFS, { type RouteMeta } from './collectRoutesFromFS.js';
import createRoutesFromMeta from './createRoutesFromMeta.js';
import createViewConfigJson from './createViewConfigJson.js';

/**
 * The URLs of the files to generate.
 */
export type RuntimeFileUrls = Readonly<{
  /**
   * The URL of the JSON file with the leaf routes and their metadata. This file
   * will be processed by the server to provide the final route configuration.
   */
  json: URL;
  /**
   * The URL of the module with the routes tree in a framework-agnostic format.
   */
  code: URL;
  /**
   * The URL of the JSON file containing server layout path information.
   */
  layouts: URL;
}>;

/**
 * Generates a file conditionally. If the file already exists and its content is the same as the
 * given data, the file will not be overwritten. It is useful to avoid unnecessary server
 * reboot during development.
 *
 * @param url - The URL of the file to generate.
 * @param data - The data to write to the file.
 */
async function generateRuntimeFile(url: URL, data: string): Promise<void> {
  await mkdir(new URL('./', url), { recursive: true });
  let contents: string | undefined;
  try {
    contents = await readFile(url, 'utf-8');
  } catch (e: unknown) {
    if (!(e != null && typeof e === 'object' && 'code' in e && e.code === 'ENOENT')) {
      throw e;
    }
  }
  if (contents !== data) {
    await writeFile(url, data, 'utf-8');
  }
}

async function applyLayouts(routeMeta: readonly RouteMeta[], layouts: URL): Promise<readonly RouteMeta[]> {
  if (!existsSync(layouts)) {
    return routeMeta;
  }
  const layoutContents = await readFile(layouts, 'utf-8');
  const availableLayouts: any[] = JSON.parse(layoutContents);
  function layoutExists(routePath: string) {
    return (
      availableLayouts.filter((layout: any) => {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access,@typescript-eslint/no-unsafe-call
        const normalizedLayout = layout.path[0] === '/' ? layout.path.substring(1) : layout.path;
        const normalizedRoute = routePath.startsWith('/') ? routePath.substring(1) : routePath;
        return normalizedRoute.startsWith(normalizedLayout);
      }).length > 0
    );
  }
  function enableFlowLayout(route: RouteMeta) {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    route.flowLayout = true;
    if (route.children) {
      // eslint-disable-next-line @typescript-eslint/no-for-in-array,no-restricted-syntax
      for (const position in route.children) {
        enableFlowLayout(route.children[position]);
      }
    }
  }

  routeMeta
    .filter((route) => route.layout === undefined && layoutExists(route.path))
    .map((route) => {
      enableFlowLayout(route);
      return route;
    });

  return routeMeta;
}

/**
 * Collects all file-based routes from the given directory, and based on them generates two files
 * described by {@link RuntimeFileUrls} type.
 * @param viewsDir - The directory that contains file-based routes (views).
 * @param urls - The URLs of the files to generate.
 * @param extensions - The list of extensions that will be collected as routes.
 * @param logger - The Vite logger instance.
 * @param debug - true to debug
 */
export async function generateRuntimeFiles(
  viewsDir: URL,
  urls: RuntimeFileUrls,
  extensions: readonly string[],
  logger: Logger,
  debug: boolean,
): Promise<void> {
  let routeMeta = existsSync(viewsDir) ? await collectRoutesFromFS(viewsDir, { extensions, logger }) : [];
  if (debug) {
    logger.info('Collected file-based routes');
  }
  routeMeta = await applyLayouts(routeMeta, urls.layouts);
  const runtimeRoutesCode = createRoutesFromMeta(routeMeta, urls);
  const viewConfigJson = await createViewConfigJson(routeMeta);

  await Promise.all([
    generateRuntimeFile(urls.json, viewConfigJson).then(() => {
      if (debug) {
        logger.info(`Frontend route list is generated: ${String(urls.json)}`);
      }
    }),
    generateRuntimeFile(urls.code, runtimeRoutesCode).then(() => {
      if (debug) {
        logger.info(`File Route module is generated: ${String(urls.code)}`);
      }
    }),
  ]);
}
