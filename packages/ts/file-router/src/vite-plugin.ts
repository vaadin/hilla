import { basename } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { TransformResult } from 'rollup';
import type { EnvironmentModuleNode, HotUpdateOptions, Logger, Plugin } from 'vite';
import { generateRuntimeFiles, type RuntimeFileUrls } from './vite-plugin/generateRuntimeFiles.js';

/**
 * Creates a code snippet that excludes the `config` export of a route file
 * from the React Fast Refresh boundary validation.
 *
 * React Fast Refresh (the HMR mechanism of `@vitejs/plugin-react`) requires
 * all exports of a module to be React components; otherwise, an update
 * invalidates the module and causes a full page reload. Route files, however,
 * are allowed to export a static `config` object in addition to the React
 * component. To keep Fast Refresh working for them, each route file is
 * registered in the `__getReactRefreshIgnoredExports` hook of the React
 * Refresh runtime, so that its `config` export is skipped during the refresh
 * boundary validation. Updates to the `config` contents are propagated
 * separately through the regenerated `file-routes.ts` module.
 *
 * @param id - The module id of the route file, as seen by the Vite transform
 * pipeline. The React Refresh runtime receives the same id at runtime.
 */
function createReactRefreshConfigExclusion(id: string): string {
  return `;if (typeof window !== 'undefined') {
  const views = (window.__HILLA_FILE_ROUTER_VIEWS__ ??= new Set());
  views.add(${JSON.stringify(id)});
  if (!window.__HILLA_FILE_ROUTER_REFRESH_HOOK_INSTALLED__) {
    window.__HILLA_FILE_ROUTER_REFRESH_HOOK_INSTALLED__ = true;
    const previousHook = window.__getReactRefreshIgnoredExports;
    window.__getReactRefreshIgnoredExports = ({ id }) => {
      const ignoredExports = previousHook?.({ id }) ?? [];
      return views.has(id) ? [...ignoredExports, 'config'] : ignoredExports;
    };
  }
}
`;
}

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
          const [path] = id.split('?');
          if (extensions.some((ext) => path!.endsWith(ext))) {
            modifiedCode += createReactRefreshConfigExclusion(id);
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
