import { opendir, readFile } from 'node:fs/promises';
import { basename, extname, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import type { Logger } from 'vite';
import { cleanUp } from './utils.js';

export type RouteMeta = Readonly<{
  path: string;
  file?: URL;
  layout?: URL;
  children?: readonly RouteMeta[];
}>;

/**
 * Routes collector options.
 */
export type CollectRoutesOptions = Readonly<{
  /**
   * The list of extensions for files that will be collected as routes.
   */
  extensions: readonly string[];
  /**
   * The parent directory of the current directory. This is a
   * nested parameter used inside the function only.
   */
  parent?: URL;
  /**
   * The Vite logger instance.
   */
  logger: Logger;
}>;

async function checkFile(url: URL | undefined, logger: Logger): Promise<URL | undefined> {
  if (url) {
    const contents = await readFile(url, 'utf-8');
    if (contents.trim() === '') {
      return undefined;
    } else if (!contents.includes('export default')) {
      logger.error(`The file "${String(url)}" should contain a default export of a component`);
      return undefined;
    }
  }

  return url;
}

const collator = new Intl.Collator('en-US');

const warningFor = ['.ts', '.js'];

/**
 * Collect route metadata from the file system and build a route tree.
 *
 * It accepts files that start with `@` as special files.
 * - `@layout` contains a component that wraps the child components.
 * - `@index` contains a component that will be used as the index page of the directory.
 *
 * It accepts files that start with `_` as private files. They will be ignored.
 *
 * @param dir - The directory to collect routes from.
 * @param options - The options object.
 *
 * @returns The route metadata array.
 */
export default async function collectRoutesFromFS(
  dir: URL,
  { extensions, logger, parent = dir }: CollectRoutesOptions,
): Promise<readonly RouteMeta[]> {
  const path = relative(fileURLToPath(parent), fileURLToPath(dir));
  let children: RouteMeta[] = [];
  let layout: URL | undefined;

  for await (const d of await opendir(dir)) {
    if (d.name.startsWith('_')) {
      continue;
    }

    if (d.isDirectory()) {
      const directoryRoutes = await collectRoutesFromFS(new URL(`${d.name}/`, dir), {
        extensions,
        logger,
        parent: dir,
      });
      if (directoryRoutes.length === 1 && directoryRoutes[0].layout) {
        const [layoutRoute] = directoryRoutes;
        children.push(layoutRoute);
      } else if (directoryRoutes.length > 0) {
        children.push({ path: d.name, children: directoryRoutes });
      }
      continue;
    }

    if (!extensions.includes(extname(d.name))) {
      if (warningFor.includes(extname(d.name))) {
        logger.warn(
          `File System based router expects only JSX files in 'Frontend/views/' directory, such as '*.tsx' and '*.jsx'. The file '${d.name}' will be ignored by router, as it doesn't match this convention. Please consider storing it in another directory.`,
        );
      }
      continue;
    }

    const file = new URL(d.name, dir);
    const url = await checkFile(file, logger);
    if (url === undefined) {
      continue;
    }
    const name = basename(d.name, extname(d.name));

    if (name === '@layout') {
      layout = file;
    } else if (name === '@index') {
      children.push({
        path: '',
        file,
      });
    } else if (name.startsWith('@')) {
      throw new Error(
        'Symbol "@" is reserved for special directories and files; only "@layout" and "@index" are allowed',
      );
    } else {
      children.push({
        path: name,
        file,
      });
    }
  }

  [children, layout] = await Promise.all([
    Promise.all(
      children.map(async (child) => ({
        ...child,
        file: child.file,
        layout: await checkFile(child.layout, logger),
      })),
    ),
    checkFile(layout, logger),
  ]);

  children = children.sort(({ path: a }, { path: b }) => collator.compare(cleanUp(a), cleanUp(b)));

  // If a layout was found, wrap the other routes with the layout route.
  return layout ? [{ path, layout, children }] : children;
}
