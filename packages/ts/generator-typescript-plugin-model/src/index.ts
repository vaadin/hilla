import Plugin from '@vaadin/generator-typescript-core/Plugin.js';
import { isEnumSchema } from '@vaadin/generator-typescript-core/Schema.js';
import type SharedStorage from '@vaadin/generator-typescript-core/SharedStorage';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { SourceFile } from 'typescript';
import { ModelEntityProcessor } from './ModelEntityProcessor.js';

export enum ModelPluginSourceType {
  Model = 'model',
}

export default class ModelPlugin extends Plugin {
  public static readonly MODEL_PLUGIN_FILE_TAGS = 'MODEL_PLUGIN_FILE_TAGS';
  public declare ['constructor']: typeof ModelPlugin;
  readonly #tags = new WeakMap<SourceFile, ModelPluginSourceType>();

  public override get path(): string {
    return import.meta.url;
  }

  public async execute(storage: SharedStorage): Promise<void> {
    const files = this.#processEntities(storage.api.components?.schemas);
    files.forEach((file) => this.#tags.set(file, ModelPluginSourceType.Model));
    storage.sources.push(...files);
    storage.pluginStorage.set(this.constructor.MODEL_PLUGIN_FILE_TAGS, this.#tags);
  }

  #processEntities(schemas: ReadonlyDeep<OpenAPIV3.ComponentsObject>['schemas'] | undefined): readonly SourceFile[] {
    this.logger.debug('Processing entities');

    return schemas
      ? Object.entries(schemas)
          .filter(([, component]) => !isEnumSchema(component))
          .map(([name, component]) => new ModelEntityProcessor(name, component, this).process())
      : [];
  }
}
