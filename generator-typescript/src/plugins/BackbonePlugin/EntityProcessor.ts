import type { OpenAPIV3 } from 'openapi-types';
import type Pino from 'pino';
import type { SourceMap } from '../../core/SharedStorage';
import type { Info, MapEntry } from './utils';

export type EntityInfo = Info<OpenAPIV3.SchemaObject>;
export type EntityInfoEntry = MapEntry<EntityInfo>;

export class EntityProcessor {
  readonly #files: SourceMap;

  readonly #info: EntityInfo;

  readonly #name: string;

  public constructor([name, info]: EntityInfoEntry, files: SourceMap, logger: Pino.Logger) {
    this.#files = files;
    this.#info = info;
    this.#name = name;
  }

  public process(): void {}
}
