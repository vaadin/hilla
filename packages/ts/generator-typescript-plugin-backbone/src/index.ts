import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage';
import type { SourceFile } from 'typescript';
import EndpointProcessor from './EndpointProcessor.js';
import { EntityProcessor } from './EntityProcessor.js';

export enum BackbonePluginSourceType {
  Endpoint = 'endpoint',
  Entity = 'entity',
}

export default class BackbonePlugin extends Plugin {
  public static readonly BACKBONE_PLUGIN_FILE_TAGS = 'BACKBONE_PLUGIN_FILE_TAGS';
  public declare ['constructor']: typeof BackbonePlugin;
  readonly #tags = new WeakMap<SourceFile, BackbonePluginSourceType>();

  public override get path(): string {
    return import.meta.url;
  }

  public async execute(storage: SharedStorage): Promise<void> {
    const endpointSourceFiles = this.#processEndpoints(storage);
    const entitySourceFiles = this.#processEntities(storage);

    endpointSourceFiles.forEach((file) => this.#tags.set(file, BackbonePluginSourceType.Endpoint));
    entitySourceFiles.forEach((file) => this.#tags.set(file, BackbonePluginSourceType.Entity));

    storage.sources.push(...endpointSourceFiles, ...entitySourceFiles);
    storage.pluginStorage.set(this.constructor.BACKBONE_PLUGIN_FILE_TAGS, this.#tags);
  }

  #processEndpoints(storage: SharedStorage): readonly SourceFile[] {
    this.logger.debug('Processing endpoints');
    const endpoints = new Map<string, EndpointProcessor>();

    for (const [path, pathItem] of Object.entries(storage.api.paths)) {
      if (!pathItem) {
        // eslint-disable-next-line no-continue
        continue;
      }

      const [, endpointName, endpointMethodName] = path.split('/');

      let endpointProcessor: EndpointProcessor;

      if (endpoints.has(endpointName)) {
        endpointProcessor = endpoints.get(endpointName)!;
      } else {
        endpointProcessor = new EndpointProcessor(endpointName, this);
        endpoints.set(endpointName, endpointProcessor);
      }

      endpointProcessor.add(endpointMethodName, pathItem);
    }

    return Array.from(endpoints.values(), (processor) => processor.process());
  }

  #processEntities(storage: SharedStorage): readonly SourceFile[] {
    this.logger.debug('Processing entities');

    return storage.api.components?.schemas
      ? Object.entries(storage.api.components?.schemas).map(([name, component]) =>
          new EntityProcessor(name, component, this).process(),
        )
      : [];
  }
}
