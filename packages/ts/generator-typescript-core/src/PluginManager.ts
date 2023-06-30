import type LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import type Plugin from './Plugin.js';
import type { PluginConstructor } from './Plugin.js';
import type ReferenceResolver from './ReferenceResolver.js';
import type SharedStorage from './SharedStorage.js';

export default class PluginManager {
  readonly #plugins: Plugin[];

  constructor(plugins: readonly PluginConstructor[], resolver: ReferenceResolver, logger: LoggerFactory) {
    logger.global.info(`Plugins: ${plugins.map(({ name }) => name).join(', ')}`);
    this.#plugins = plugins.map((PluginClass) => new PluginClass(resolver, logger));
  }

  async execute(storage: SharedStorage): Promise<void> {
    // We need to run plugins sequentially
    for (const plugin of this.#plugins) {
      const { name, path } = plugin;
      plugin.logger.debug({ plugin: { name, path } }, `Executing plugin '${plugin.name}'`);
      // eslint-disable-next-line no-await-in-loop
      await plugin.execute(storage);
    }
  }
}
