import Plugin from '@vaadin/generator-typescript-core/Plugin.js';
import type ReferenceResolver from '@vaadin/generator-typescript-core/ReferenceResolver';
import type SharedStorage from '@vaadin/generator-typescript-core/SharedStorage';
import type Pino from 'pino';
import type { SourceFile } from 'typescript';
import type { ModelPluginContext } from './utils.js';
import { ModelEntityProcessor } from './ModelEntityProcessor.js';

export enum ModelPluginSourceType {
  Model = 'model',
}

export default class ModelPlugin extends Plugin {
  public static readonly MODEL_PLUGIN_FILE_TAGS = 'MODEL_PLUGIN_FILE_TAGS';
  public declare ['constructor']: typeof ModelPlugin;
  readonly #context: ModelPluginContext;
  readonly #tags = new WeakMap<SourceFile, ModelPluginSourceType>();

  public constructor(resolver: ReferenceResolver, logger: Pino.Logger) {
    super(resolver, logger);
    this.#context = {
      logger,
      resolver,
    };
  }

  public override get path(): string {
    return import.meta.url;
  }

  public async execute(storage: SharedStorage): Promise<void> {
    const modelSourceFiles = this.#processEntities(storage);
    modelSourceFiles.forEach((file) => this.#tags.set(file, ModelPluginSourceType.Model));
    storage.sources.push(...modelSourceFiles);
    storage.pluginStorage.set(this.constructor.MODEL_PLUGIN_FILE_TAGS, this.#tags);
  }

  #processEntities(storage: SharedStorage): readonly SourceFile[] {
    this.logger.debug('Processing entities');

    return storage.api.components?.schemas
      ? Object.entries(storage.api.components?.schemas).map(([name, component]) =>
          new ModelEntityProcessor(name, component, this.#context).process(),
        )
      : [];
  }
}
