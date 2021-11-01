import type Pino from 'pino';
import type { SourceFile } from 'typescript';
import Plugin from '../../core/Plugin';
import type ReferenceResolver from '../../core/ReferenceResolver';
import type SharedStorage from '../../core/SharedStorage';
import EndpointProcessor from './EndpointProcessor';
import type { BackbonePluginContext } from './utils';

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
    // this.#processEntities(storage);

    storage.sources.push(...endpointSourceFiles);
  }

  #processEndpoints(storage: SharedStorage): readonly SourceFile[] {
    const endpoints = new Map<string, EndpointProcessor>();

    for (const [path, pathItem] of Object.entries(storage.api.paths)) {
      if (!pathItem) {
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

  // #processEntities(storage: SharedStorage): void {
  //   if (storage.api.components?.schemas) {
  //     for (const schema of Object.entries(storage.api.components.schemas)) {
  //       new EntityProcessor(schema as EntityInfoEntry, files).process();
  //     }
  //   }
  // }
}
