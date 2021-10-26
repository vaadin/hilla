import { OpenAPIV3 } from 'openapi-types';
import type Pino from 'pino';
import type { ReadonlyDeep } from 'type-fest';
import type { Statement } from 'typescript';
import type SharedStorage from '../../core/SharedStorage';
import EndpointMethodOperationProcessor, { EndpointMethodOperation } from './EndpointMethodOperationProcessor';
import { EndpointMethodContext } from './utils';

export type EndpointMethodObject = ReadonlyDeep<OpenAPIV3.PathItemObject>;
export type EndpointMethodProcessorEntry = readonly [path: string, info: EndpointMethodObject];

export class EndpointMethodProcessor {
  readonly #info: EndpointMethodObject;
  readonly #context: EndpointMethodContext;
  readonly #logger: Pino.Logger;

  public constructor([path, info]: EndpointMethodProcessorEntry, storage: SharedStorage, logger: Pino.Logger) {
    this.#context = this.#createContext(path, storage);
    this.#info = info;
    this.#logger = logger;
  }

  public process(): void {
    this.#logger.info(
      { endpoint: this.#context.endpoint, method: this.#context.endpointMethod },
      'Start processing endpoint method'
    );

    for (const method of Object.values(OpenAPIV3.HttpMethods)) {
      if (this.#info[method]) {
        new EndpointMethodOperationProcessor(
          method,
          this.#info[method] as EndpointMethodOperation,
          this.#context,
          this.#logger
        ).process();
      }
    }
  }

  #createContext(path: string, storage: SharedStorage): EndpointMethodContext {
    const [, endpoint, method] = path.split('/');

    let source: Statement[];

    if (storage.sources.has(endpoint)) {
      source = storage.sources.get(endpoint)!;
    } else {
      source = [];
      storage.sources.set(endpoint, source);
    }

    return {
      endpoint,
      endpointMethod: method,
      source,
      storage,
    };
  }
}
