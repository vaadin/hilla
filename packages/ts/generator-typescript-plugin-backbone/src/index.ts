import type Pino from 'pino';
import type { SourceFile } from 'typescript';
import Plugin from '@vaadin/generator-typescript-core/Plugin.js';
import type ReferenceResolver from '@vaadin/generator-typescript-core/ReferenceResolver';
import type SharedStorage from '@vaadin/generator-typescript-core/SharedStorage';
import BarrelProcessor from './BarrelProcessor.js';
import EndpointProcessor from './EndpointProcessor.js';
import { EntityProcessor } from './EntityProcessor.js';
import type { BackbonePluginContext } from './utils.js';

export default class BackbonePlugin extends Plugin {
  readonly #context: BackbonePluginContext;

  public constructor(resolver: ReferenceResolver, logger: Pino.Logger) {
    super(resolver, logger);
    this.#context = {
      logger,
      resolver,
    };
  }

  public get path(): string {
    return import.meta.url;
  }

  public async execute(storage: SharedStorage): Promise<void> {
    const endpointSourceFiles = this.#processEndpoints(storage);
    const entitySourceFiles = this.#processEntities(storage);
    const barrelFile = new BarrelProcessor(endpointSourceFiles).process();

    storage.sources.push(barrelFile, ...endpointSourceFiles, ...entitySourceFiles);
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
        endpointProcessor = new EndpointProcessor(endpointName, this.#context);
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
          new EntityProcessor(name, component, this.#context).process(),
        )
      : [];
  }
}
