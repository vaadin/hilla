import type Pino from 'pino';
import type Plugin from './Plugin.js';
import type { PluginConstructor } from './Plugin.js';
import type ReferenceResolver from './ReferenceResolver.js';
import type SharedStorage from './SharedStorage.js';

export default class PluginManager {
  readonly #logger: Pino.Logger;
  readonly #plugins: Plugin[];
  readonly #resolver: ReferenceResolver;

  public constructor(plugins: readonly PluginConstructor[], resolver: ReferenceResolver, logger: Pino.Logger) {
    this.#logger = logger;
    this.#resolver = resolver;

    this.#logger.info(`Plugins: ${plugins.map(({ name }) => name).join(', ')}`);

    this.#plugins = plugins.map((PluginClass) => new PluginClass(this.#resolver, this.#logger));
  }

  public async execute(storage: SharedStorage): Promise<void> {
    // We need to run plugins sequentially
    for (const plugin of this.#plugins) {
      const { name, path } = plugin;
      this.#logger.debug({ name, path }, `Executing plugin '${plugin.name}'`);
      // eslint-disable-next-line no-await-in-loop
      await plugin.execute(storage);
    }
  }
}
