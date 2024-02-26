import { fileURLToPath, pathToFileURL } from 'node:url';
import type { Logger, Plugin } from 'vite';
import { generateRuntimeFiles, type RuntimeFileUrls } from './vite-plugin/generateRuntimeFiles.js';

/**
 * The options for the Vite file-based router plugin.
 */
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
}>;

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
  let _outDir: URL;
  let _logger: Logger;
  let runtimeUrls: RuntimeFileUrls;

  return {
    name: 'vite-plugin-file-router',
    configResolved({ logger, root, build: { outDir } }) {
      const _root = pathToFileURL(root);
      const _generatedDir = new URL(generatedDir, _root);

      _viewsDir = new URL(viewsDir, _root);
      _outDir = pathToFileURL(outDir);
      _logger = logger;

      _logger.info(`The directory of route files: ${String(_viewsDir)}`);
      _logger.info(`The directory of generated files: ${String(_generatedDir)}`);
      _logger.info(`The output directory: ${String(_outDir)}`);

      runtimeUrls = {
        json: new URL('views.json', _outDir),
        code: new URL('views.ts', _generatedDir),
      };
    },
    async buildStart() {
      try {
        await generateRuntimeFiles(_viewsDir, runtimeUrls, extensions, _logger);
      } catch (e: unknown) {
        _logger.error(String(e));
      }
    },
    configureServer(server) {
      const dir = fileURLToPath(_viewsDir);

      const changeListener = (file: string): void => {
        if (!file.startsWith(dir)) {
          return;
        }

        generateRuntimeFiles(_viewsDir, runtimeUrls, extensions, _logger).catch((e: unknown) =>
          _logger.error(String(e)),
        );
      };

      server.watcher.on('add', changeListener);
      server.watcher.on('change', changeListener);
      server.watcher.on('unlink', changeListener);
    },
  };
}
