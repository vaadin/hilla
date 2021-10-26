import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import Plugin from '../../core/Plugin';
import type SharedStorage from '../../core/SharedStorage';
import type { SourceMap } from '../../core/SharedStorage';
import { EndpointMethodProcessor } from './EndpointMethodProcessor';
import { EntityProcessor } from './EntityProcessor';
import type { EntityInfoEntry } from './EntityProcessor';

export default class BackbonePlugin extends Plugin {
  public get path(): string {
    return import.meta.url;
  }

  public async execute(storage: SharedStorage): Promise<void> {
    this.#processEndpoints(storage);
    this.#processEntities(storage);
  }

  #processEndpoints(storage: SharedStorage): void {
    for (const entry of Object.entries(storage.api.paths).filter(([, info]) => !!info)) {
      new EndpointMethodProcessor(entry, storage).process();
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
