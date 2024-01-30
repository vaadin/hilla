import { writeFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
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
  extensions?: readonly string[];
}>;

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
  const json = await generateJson(routeMeta);

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
}: PluginOptions = {}): Plugin {
  let _viewsDir: URL;
  let _generatedDir: URL;
  let _outDir: URL;
  let generatedUrls: GeneratedUrls;

  return {
    name: 'vite-plugin-file-router',
    configResolved({ root, build: { outDir } }) {
      const _root = pathToFileURL(root);
      _viewsDir = new URL(viewsDir, _root);
      _generatedDir = new URL(generatedDir, _root);
      _outDir = pathToFileURL(outDir);
      generatedUrls = {
        json: new URL('views.json', _outDir),
        code: new URL('views.ts', _generatedDir),
      };
    },
    async buildStart() {
      await build(_viewsDir, _generatedDir, generatedUrls, extensions);
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
