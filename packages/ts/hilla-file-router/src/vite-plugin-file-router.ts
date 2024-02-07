import { writeFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { Plugin } from 'vite';
import collectRoutesFromFS from './vite-plugin/collectRoutesFromFS.js';
import createRoutesFromMeta from './vite-plugin/createRoutesFromMeta.js';
import createViewConfigJson from './vite-plugin/createViewConfigJson.js';

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
   * The list of extensions that will be collected as routes of the file-based
   * router.
   *
   * @defaultValue `['.tsx', '.jsx', '.ts', '.js']`
   */
  extensions?: readonly string[];
  /**
   * The name of the export that will be used for the {@link ViewConfig} in the
   * route file.
   *
   * @defaultValue `config`
   */
  configExportName?: string;
}>;

type RuntimeFileUrls = Readonly<{
  json: URL;
  code: URL;
}>;

async function generateRuntimeFiles(code: string, json: string, urls: RuntimeFileUrls) {
  await Promise.all([writeFile(urls.json, json, 'utf-8'), writeFile(urls.code, code, 'utf-8')]);
}

async function build(
  viewsDir: URL,
  outDir: URL,
  generatedUrls: RuntimeFileUrls,
  extensions: readonly string[],
  configExportName: string,
): Promise<void> {
  const routeMeta = await collectRoutesFromFS(viewsDir, { extensions });
  const runtimeRoutesCode = createRoutesFromMeta(routeMeta, outDir);
  const viewConfigJson = await createViewConfigJson(routeMeta, configExportName);

  await generateRuntimeFiles(runtimeRoutesCode, viewConfigJson, generatedUrls);
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
  configExportName = 'config',
}: PluginOptions = {}): Plugin {
  let _viewsDir: URL;
  let _generatedDir: URL;
  let _outDir: URL;
  let generatedUrls: RuntimeFileUrls;

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
      await build(_viewsDir, _generatedDir, generatedUrls, extensions, configExportName);
    },
    configureServer(server) {
      const dir = fileURLToPath(_viewsDir);

      const changeListener = (file: string): void => {
        if (!file.startsWith(dir)) {
          return;
        }

        build(_viewsDir, _outDir, generatedUrls, extensions, configExportName).catch((error) => console.error(error));
      };

      server.watcher.on('add', changeListener);
      server.watcher.on('change', changeListener);
      server.watcher.on('unlink', changeListener);
    },
  };
}
