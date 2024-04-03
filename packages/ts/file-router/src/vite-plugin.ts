import { basename } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { TransformResult } from 'rollup';
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
   * @defaultValue `['.tsx', '.jsx']`
   */
  extensions?: readonly string[];
  /**
   * The flag to indicate whether the plugin is running in development mode.
   *
   * @defaultValue `false`
   */
  isDevMode?: boolean;
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
  extensions = ['.tsx', '.jsx'],
  isDevMode = false,
}: PluginOptions = {}): Plugin {
  const hmrInjectionPattern = /(?<=import\.meta\.hot\.accept[\s\S]+)if\s\(!nextExports\)\s+return;/u;

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
        json: new URL('file-routes.json', isDevMode ? _generatedDir : _outDir),
        code: new URL('file-routes.ts', _generatedDir),
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

        generateRuntimeFiles(_viewsDir, runtimeUrls, extensions, _logger)
          .then(() => server.hot.send({ type: 'full-reload' }))
          .catch((e: unknown) => _logger.error(String(e)));
      };

      server.watcher.on('add', changeListener);
      server.watcher.on('change', changeListener);
      server.watcher.on('unlink', changeListener);
    },
    transform(code, id): Promise<TransformResult> | TransformResult {
      if (id.startsWith(fileURLToPath(_viewsDir)) && !basename(id).startsWith('_')) {
        // To enable HMR for route files with exported configurations, we need
        // to address a limitation in `react-refresh`. This library requires
        // strict equality (`===`) for non-component exports. However, the
        // dynamic nature of HMR makes maintaining this equality between object
        // literals challenging.
        //
        // To work around this, we implement a strategy that preserves the
        // reference to the original configuration object (`currentExports.config`),
        // replacing any newly created configuration objects (`nextExports.config`)
        // with it. This ensures that the HMR mechanism perceives the
        // configuration as unchanged.
        return {
          code: code.replace(
            hmrInjectionPattern,
            `if (!nextExports) return;
      if (Object.keys(nextExports).length === 2 && 'default' in nextExports && 'config' in nextExports) {
        nextExports = { ...nextExports, config: currentExports.config };
      }`,
          ),
        };
      }

      return undefined;
    },
  };
}
