import { relative, resolve } from 'path';
import type Pino from 'pino';
import GeneratorError from './GeneratorException';
import Plugin, { PluginConstructor } from './Plugin';
import type ReferenceResolver from './ReferenceResolver';
import type SharedStorage from './SharedStorage';

export type PluginsConfiguration = Readonly<{
  disable: readonly string[];
  use: readonly string[];
}>;

const cwd = process.cwd();

const builtInPluginPaths: readonly string[] = [resolve(import.meta.url, '../plugins/BackbonePlugin')];

function absolutize(paths?: readonly string[]): readonly string[] {
  return paths ? Array.from(new Set(paths), (path) => resolve(cwd, path)) : [];
}

async function importPlugin(path: string, resolver: ReferenceResolver, logger: Pino.Logger): Promise<Plugin> {
  const PluginClass: PluginConstructor = (await import(path)).default;

  if (!Object.prototype.isPrototypeOf.call(Plugin, PluginClass)) {
    const error = new GeneratorError(`Plugin '${relative(cwd, path)}' is not an instance of a Plugin class`);
    logger.error(error, 'Error during plugin import');
    throw error;
  }

  return new PluginClass(resolver, logger);
}

export default class PluginManager {
  public static async init(
    plugins: PluginsConfiguration | undefined,
    resolver: ReferenceResolver,
    logger: Pino.Logger
  ): Promise<PluginManager> {
    const disabledPluginsPaths = absolutize(plugins?.disable);
    const userDefinedPlugins = absolutize(plugins?.use);

    logger.info({ paths: disabledPluginsPaths }, 'Disabled built-in plugins');
    logger.info({ paths: userDefinedPlugins }, 'User-defined plugins');

    const builtInPlugins = builtInPluginPaths.filter((path) => !disabledPluginsPaths.includes(path));

    const importedPlugins = await Promise.all(
      [...builtInPlugins, ...userDefinedPlugins].map((path) => importPlugin(path, resolver, logger))
    );

    return new PluginManager(importedPlugins, logger);
  }

  readonly #logger: Pino.Logger;

  readonly #plugins: readonly Plugin[];

  private constructor(plugins: readonly Plugin[], logger: Pino.Logger) {
    this.#logger = logger;
    this.#plugins = plugins;
  }

  public async execute(storage: SharedStorage): Promise<void> {
    // We need to run plugins sequentially
    for (const plugin of this.#plugins) {
      const { name, path } = plugin;
      this.#logger.info({ name, path }, `Executing plugin '${plugin.name}'`);
      // eslint-disable-next-line no-await-in-loop
      await plugin.execute(storage);
    }
  }
}
