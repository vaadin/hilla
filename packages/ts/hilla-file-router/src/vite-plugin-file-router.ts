import { opendir, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import type { Writable } from 'type-fest';
import type { Plugin } from 'vite';
import collectRoutes from './collectRoutes.js';
import generateJson from './generateJson.js';
import generateRoutes from './generateRoutes.js';

export type PluginOptions = Readonly<{
  /**
   * The base directory for the router. The folders and files in this directory
   * will be used as route paths.
   *
   * @defaultValue `frontend/views`
   */
  viewsDir?: URL | string;
  /**
   * The directory where the generated view file will be stored.
   *
   * @defaultValue `frontend/generated`
   */
  generatedDir?: URL | string;
  /**
   * The list of extensions that will be collected as routes of the file-based router.
   *
   * @defaultValue `['.tsx', '.jsx', '.ts', '.js']`
   */
  extensions: readonly string[];
}>;

type RouteData = Readonly<{
  pattern: string;
  file?: URL;
}>;

async function* walk(
  dir: URL,
  parents: readonly RouteData[],
): AsyncGenerator<readonly RouteData[], undefined, undefined> {
  for await (const d of await opendir(dir)) {
    const entry = new URL(d.name, dir);
    if (d.isDirectory()) {
      yield* walk(entry, [...parents, { pattern: d.name }]);
    } else if (d.isFile()) {
      if (d.name.startsWith('layout')) {
        if (parents.length > 0) {
          (parents.at(-1)! as Writable<RouteData>).file = entry;
        }
      } else {
        yield [...parents, { pattern: d.name, file: entry }];
      }
    }
  }
}

type GeneratedUrls = Readonly<{
  json: URL;
  code: URL;
}>;

async function generate(code: string, json: string, urls: GeneratedUrls) {
  await Promise.all([writeFile(urls.json, json, 'utf-8'), writeFile(urls.code, code, 'utf-8')]);
}

async function build(
  viewsDir: URL,
  outDir: URL,
  generatedUrls: GeneratedUrls,
  extensions: readonly string[],
): Promise<void> {
  const routeMeta = await collectRoutes(viewsDir, { extensions });
  const code = generateRoutes(routeMeta, outDir);
  const json = generateJson(routeMeta);

  await generate(code, json, generatedUrls);
}

/**
 * A Vite plugin that generates a router from the files in the specific directory.
 *
 * @param options - The plugin options.
 * @returns A Vite plugin.
 */
export default function vitePluginFileSystemRouter({
  viewsDir = 'frontend/views/',
  generatedDir = 'frontend/generated/',
  extensions = ['.tsx', '.jsx', '.ts', '.js'],
}: PluginOptions): Plugin {
  let _viewsDir: URL;
  let _generatedDir: URL;
  let _outDir: URL;
  let generatedUrls: GeneratedUrls;

  return {
    name: 'vite-plugin-file-router',
    configResolved({ root, build: { outDir } }) {
      const _root = new URL(root);
      _viewsDir = new URL(viewsDir, _root);
      _generatedDir = new URL(generatedDir, _root);
      _outDir = new URL(outDir, _root);
      generatedUrls = {
        json: new URL('views.json', _outDir),
        code: new URL('views.ts', _generatedDir),
      };
    },
    async buildStart() {
      await build(_viewsDir, _outDir, generatedUrls, extensions);
    },
    configureServer(server) {
      const dir = fileURLToPath(_viewsDir);

      server.watcher.on('unlink', (file) => {
        if (!file.startsWith(dir)) {
          return;
        }

        build(_viewsDir, _outDir, generatedUrls, extensions).catch((error) => console.error(error));
      });
    },
  };
}
