import { mkdir, writeFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { Logger, Plugin } from 'vite';
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
}>;

type RuntimeFileUrls = Readonly<{
  json: URL;
  code: URL;
}>;

async function generateRuntimeFiles(code: string, json: string, urls: RuntimeFileUrls, logger: Logger) {
  await Promise.all([
    mkdir(new URL('./', urls.code), { recursive: true }),
    mkdir(new URL('./', urls.json), { recursive: true }),
  ]);
  await Promise.all([
    writeFile(urls.json, json, 'utf-8').then(() =>
      logger.info(`Frontend route list is generated: ${String(urls.json)}`),
    ),
    writeFile(urls.code, code, 'utf-8').then(() => logger.info(`Views module is generated: ${String(urls.code)}`)),
  ]);
}

async function build(
  viewsDir: URL,
  outDir: URL,
  generatedUrls: RuntimeFileUrls,
  extensions: readonly string[],
  logger: Logger,
): Promise<void> {
  const routeMeta = await collectRoutesFromFS(viewsDir, { extensions });
  logger.info('Collected file-based routes');
  const runtimeRoutesCode = createRoutesFromMeta(routeMeta, outDir);
  const viewConfigJson = await createViewConfigJson(routeMeta);

  await generateRuntimeFiles(runtimeRoutesCode, viewConfigJson, generatedUrls, logger);
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
  let _logger: Logger;
  let generatedUrls: RuntimeFileUrls;

  return {
    name: 'vite-plugin-file-router',
    configResolved({ logger, root, build: { outDir } }) {
      const _root = pathToFileURL(root);
      _viewsDir = new URL(viewsDir, _root);
      _generatedDir = new URL(generatedDir, _root);
      _outDir = pathToFileURL(outDir);
      _logger = logger;

      _logger.info(`The directory of route files: ${String(_viewsDir)}`);
      _logger.info(`The directory of generated files: ${String(_generatedDir)}`);
      _logger.info(`The output directory: ${String(_outDir)}`);

      generatedUrls = {
        json: new URL('views.json', _outDir),
        code: new URL('views.ts', _generatedDir),
      };
    },
    async buildStart() {
      try {
        await build(_viewsDir, _generatedDir, generatedUrls, extensions, _logger);
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

        build(_viewsDir, _generatedDir, generatedUrls, extensions, _logger).catch((e: unknown) =>
          _logger.error(String(e)),
        );
      };

      server.watcher.on('add', changeListener);
      server.watcher.on('change', changeListener);
      server.watcher.on('unlink', changeListener);
    },
  };
}
