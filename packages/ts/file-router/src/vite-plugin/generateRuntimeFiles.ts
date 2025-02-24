import { mkdir, readFile, writeFile } from 'node:fs/promises';
import type { Logger } from 'vite';
import applyLayouts from './applyLayouts.js';
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
 * @param forceWrite - true to force writing the file even if there are no changes
 * @returns true if the file was written, false otherwise.
 */
async function generateRuntimeFile(url: URL, data: string, forceWrite?: boolean): Promise<boolean> {
  await mkdir(new URL('./', url), { recursive: true });
  let shouldWrite = forceWrite ?? false;
  if (!forceWrite) {
    let contents: string | undefined;
    try {
      contents = await readFile(url, 'utf-8');
    } catch (e: unknown) {
      if (!(e != null && typeof e === 'object' && 'code' in e && e.code === 'ENOENT')) {
        throw e;
      }
    }
    shouldWrite = contents !== data;
  }
  if (shouldWrite) {
    await writeFile(url, data, 'utf-8');
  }

  return shouldWrite;
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
// eslint-disable-next-line @typescript-eslint/max-params
export async function generateRuntimeFiles(
  viewsDir: URL,
  urls: RuntimeFileUrls,
  extensions: readonly string[],
  logger: Logger,
  debug: boolean,
): Promise<void> {
  let routeMeta: readonly RouteMeta[];
  try {
    routeMeta = await collectRoutesFromFS(viewsDir, { extensions, logger });
  } catch (e: unknown) {
    if (e instanceof Error && 'code' in e && e.code === 'ENOENT') {
      routeMeta = [];
    } else {
      throw e;
    }
  }

  if (debug) {
    logger.info('Collected file-based routes');
  }
  routeMeta = await applyLayouts(routeMeta, urls.layouts);
  const runtimeRoutesCode = createRoutesFromMeta(routeMeta, urls);
  const viewConfigJson = await createViewConfigJson(routeMeta);

  const jsonWritten = await generateRuntimeFile(urls.json, viewConfigJson);
  if (debug) {
    logger.info(`Frontend route list is generated: ${String(urls.json)}`);
  }
  // If the metadata has changed, we need to write the TS file also to make Vite HMR updates work properly.
  // Even though the contents of the TS file does not change, the contents of the files imported in the TS
  // files changes and the routes must be re-applied to the React router
  await generateRuntimeFile(urls.code, runtimeRoutesCode, jsonWritten);
  if (debug) {
    logger.info(`File Route module is generated: ${String(urls.code)}`);
  }
}
