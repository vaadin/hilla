import { basename } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { TransformResult } from 'rollup';
import type { EnvironmentModuleNode, HotUpdateOptions, Logger, Plugin } from 'vite';
import { generateRuntimeFiles, type RuntimeFileUrls } from './vite-plugin/generateRuntimeFiles.js';

const INJECTION =
  "if (Object.keys(nextExports).length === 2 && 'default' in nextExports && 'config' in nextExports) {nextExports = { ...nextExports, config: currentExports.config };}";

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
  /**
   * The flag to indicate whether to output debug information
   *
   * @defaultValue `false`
   */
  debug?: boolean;
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
  debug = false,
}: PluginOptions = {}): Plugin {
  let _viewsDir: URL;
  let _outDir: URL;
  let _logger: Logger;
  let runtimeUrls: RuntimeFileUrls;
  let _generateRuntimeFiles: () => Promise<boolean>;
  let _viewsDirPosix: string;

  return {
    name: 'vite-plugin-file-router',
    enforce: 'pre',
    configResolved({ logger, root, build: { outDir } }) {
      const _root = pathToFileURL(root);
      const _generatedDir = new URL(generatedDir, _root);

      _viewsDir = new URL(viewsDir, _root);
      _viewsDirPosix = fileURLToPath(_viewsDir).replaceAll('\\', '/');
      _outDir = pathToFileURL(outDir);
      _logger = logger;

      if (debug) {
        _logger.info(`The directory of route files: ${String(_viewsDir)}`);
        _logger.info(`The directory of generated files: ${String(_generatedDir)}`);
        _logger.info(`The output directory: ${String(_outDir)}`);
      }
      runtimeUrls = {
        json: new URL('file-routes.json', isDevMode ? _generatedDir : _outDir),
        code: new URL('file-routes.ts', _generatedDir),
        layouts: new URL('layouts.json', _generatedDir),
      };
      _generateRuntimeFiles = async (): Promise<boolean> => {
        try {
          return await generateRuntimeFiles(_viewsDir, runtimeUrls, extensions, _logger, debug);
        } catch (e: unknown) {
          _logger.error(String(e));
          return true;
        }
      };
    },
    async buildStart() {
      await _generateRuntimeFiles();
    },
    async hotUpdate({ file, modules }: HotUpdateOptions): Promise<void | EnvironmentModuleNode[]> {
      const fileUrlString = String(pathToFileURL(file));

      if (fileUrlString === String(runtimeUrls.json)) {
        // Reload file routes JSON with a custom HMR event.
        this.environment.hot.send({ type: 'custom', event: 'fs-route-update' });
        return [];
      }

      if (fileUrlString === String(runtimeUrls.code)) {
        // Skip HMR for file routes from Vite builtin file change listener,
        // as we have our own HMR handling below.
        return [];
      }

      if (!file.startsWith(_viewsDirPosix)) {
        // Outside views folder, only changes to layouts file should trigger
        // files generation.
        if (fileUrlString !== String(runtimeUrls.layouts)) {
          return;
        }
      }

      // Check and update file routes if needed.
      if (await _generateRuntimeFiles()) {
        // The "file-routes.ts" file was changed at this point, so it should
        // be considered within the HMR update that caused the routes update.

        const mg = this.environment.moduleGraph;
        const fileRoutesModules = mg.getModulesByFile(fileURLToPath(runtimeUrls.code).replaceAll('\\', '/'))!;

        // Make eager update for file routes in Vite module graph
        // for consistency with the generated file contents.
        await Promise.all(
          Array.from(fileRoutesModules, async (fileRouteModule) => {
            mg.invalidateModule(fileRouteModule);
            await this.environment.fetchModule(fileRouteModule.id!, undefined, { cached: false });
          }),
        );

        const neverImported = modules.every((module) => module.importers.size === 0);
        if (neverImported) {
          // The current file is a not imported anywhere, however it caused
          // "file-routes.ts" update: possibly a route for this file
          // was removed, or we're processing "layouts.json".

          // Default Vite HMR behavior for not imported modules is a full page
          // reload. However, in this case, we could avoid it and only do HMR
          // for the file routes instead.
          return [...fileRoutesModules];
        }

        // Add "file-routes.ts" to current HMR module update list to reload
        // both at the same time.
        return [...fileRoutesModules, ...modules];
      }

      return undefined;
    },
    transform(code, id): Promise<TransformResult> | TransformResult {
      let modifiedCode = code;
      if (id.startsWith(_viewsDirPosix) && !basename(id).startsWith('_')) {
        if (isDevMode) {
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
          const injectionPattern = /import\.meta\.hot\.accept[\s\S]+if\s\(!nextExports\)\s+return;/gu;
          if (injectionPattern.test(modifiedCode)) {
            modifiedCode = `${modifiedCode.substring(0, injectionPattern.lastIndex)}${INJECTION}${modifiedCode.substring(
              injectionPattern.lastIndex,
            )}`;
          }
        } else {
          // In production mode, the function name is assigned as name to the function itself to avoid minification
          const functionNames = /export\s+default\s+(?:function\s+)?(\w+)/u.exec(modifiedCode);

          if (functionNames?.length) {
            const [, functionName] = functionNames;
            modifiedCode += `Object.defineProperty(${functionName}, 'name', { value: '${functionName}' });\n`;
          }
        }

        return {
          code: modifiedCode,
        };
      }

      return undefined;
    },
  };
}
