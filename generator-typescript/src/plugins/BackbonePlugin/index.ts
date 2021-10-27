import Pino from 'pino';
import Plugin from '../../core/Plugin';
import ReferenceResolver from '../../core/ReferenceResolver';
import type SharedStorage from '../../core/SharedStorage';
import { EndpointMethodProcessor, EndpointMethodProcessorEntry } from './EndpointMethodProcessor';
import type { EntityInfoEntry } from './EntityProcessor';
import { EntityProcessor } from './EntityProcessor';
import type { BackbonePluginContext } from './utils';

export default class BackbonePlugin extends Plugin {
  readonly #context: BackbonePluginContext;

  public constructor(resolver: ReferenceResolver, logger: Pino.Logger) {
    super(resolver, logger);
    this.#context = {
      imports: new Map(),
      logger,
      resolver,
    };
  }

  public get path(): string {
    return import.meta.url;
  }

  public async execute(storage: SharedStorage): Promise<void> {
    this.#processEndpoints(storage);
    this.#processEntities(storage);
  }

  #processEndpoints(storage: SharedStorage): void {
    for (const entry of Object.entries(storage.api.paths).filter(([, info]) => !!info)) {
      new EndpointMethodProcessor(entry as EndpointMethodProcessorEntry, storage.sources, this.#context).process();
    }
  }

  #processEntities(storage: SharedStorage): void {
    if (storage.api.components?.schemas) {
      for (const schema of Object.entries(storage.api.components.schemas)) {
        new EntityProcessor(schema as EntityInfoEntry, files).process();
      }
    }
  }
}
