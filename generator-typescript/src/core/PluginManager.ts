import { relative, resolve } from 'path';
import GeneratorError from './GeneratorException';
import Plugin from './Plugin';
import type SharedStorage from './SharedStorage';

export type Plugins = Readonly<{
  disable: readonly string[];
  use: readonly string[];
}>;

const cwd = process.cwd();

const builtInPluginPaths: readonly string[] = [resolve(import.meta.url, '../plugins/backbone-plugin')];

function absolutize(paths: readonly string[]): readonly string[] {
  return Array.from(new Set(paths), (path) => resolve(cwd, path));
}

async function importPluginClass(path: string): Promise<Plugin> {
  const pluginClass = (await import(path)).default;

  if (!Object.prototype.isPrototypeOf.call(Plugin, pluginClass)) {
    throw new GeneratorError(`Plugin '${relative(cwd, path)}' is not an instance of a Plugin class`);
  }

  return pluginClass;
}

export default class PluginManager {
  public static async init(plugins: Plugins): Promise<PluginManager> {
    const disabledPluginsPaths = absolutize(plugins.disable);
    const userDefinedPlugins = absolutize(plugins.use);

    const builtInPlugins = builtInPluginPaths.filter((path) => !disabledPluginsPaths.includes(path));

    const importedPlugins = await Promise.all([...builtInPlugins, ...userDefinedPlugins].map(importPluginClass));

    return new PluginManager(importedPlugins);
  }

  readonly #plugins: readonly Plugin[];

  private constructor(plugins: readonly Plugin[]) {
    this.#plugins = plugins;
  }

  public execute(storage: SharedStorage): void {
    for (const plugin of this.#plugins) {
      plugin.execute(storage);
    }
  }
}
