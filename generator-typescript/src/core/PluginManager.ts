import type Pino from 'pino';
import GeneratorError from './GeneratorException.js';
import Plugin, { PluginConstructor } from './Plugin.js';
import type ReferenceResolver from './ReferenceResolver';
import type SharedStorage from './SharedStorage';

export default class PluginManager {
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

  public async load(path: string): Promise<PluginConstructor> {
    const cls: PluginConstructor = (await import(path)).default;

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
