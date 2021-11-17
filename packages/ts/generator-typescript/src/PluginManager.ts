import type Pino from 'pino';
import { fileURLToPath, URL } from 'url';
import GeneratorError from './GeneratorException.js';
import Plugin, { PluginConstructor } from './Plugin.js';
import type ReferenceResolver from './ReferenceResolver';
import type SharedStorage from './SharedStorage';

export default class PluginManager {
  public static async create(
    plugins: readonly string[],
    resolver: ReferenceResolver,
    logger: Pino.Logger,
  ): Promise<PluginManager> {
    const manager = new PluginManager(resolver, logger);

    const resolvedPluginPaths: readonly URL[] = Array.from(new Set(plugins), (plugin) => new URL(plugin, cwd));

    logger.info({ paths: resolvedPluginPaths }, 'User-defined plugins');

    (await Promise.all(resolvedPluginPaths.map(async (path) => manager.load(path)))).forEach((PluginClass) =>
      manager.add(PluginClass),
    );

    return manager;
  }

  readonly #logger: Pino.Logger;
  readonly #plugins: Plugin[] = [];
  readonly #resolver: ReferenceResolver;

  public constructor(resolver: ReferenceResolver, logger: Pino.Logger) {
    this.#logger = logger;
    this.#resolver = resolver;
  }

  public add(PluginClass: PluginConstructor): void {
    this.#logger.info(`Plugin is used: '${PluginClass.name}'`);
    this.#plugins.push(new PluginClass(this.#resolver, this.#logger));
  }

  public async load(path: URL): Promise<PluginConstructor> {
    const cls: PluginConstructor = (await import(fileURLToPath(path))).default;

    if (!Object.prototype.isPrototypeOf.call(Plugin, cls)) {
      const error = new GeneratorError(`Plugin '${path}' is not an instance of a Plugin class`);
      this.#logger.error(error, 'Error during plugin import');
      throw error;
    }

    return cls;
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
