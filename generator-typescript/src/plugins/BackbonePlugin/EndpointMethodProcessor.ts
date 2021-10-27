import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { Statement } from 'typescript';
import type { SourceMap } from '../../core/SharedStorage';
import EndpointMethodOperationProcessor, { EndpointMethodOperation } from './EndpointMethodOperationProcessor';
import type { BackbonePluginContext, EndpointMethodData } from './utils';

export type EndpointMethodPathItem = ReadonlyDeep<OpenAPIV3.PathItemObject>;
export type EndpointMethodProcessorEntry = readonly [path: string, info: EndpointMethodPathItem];

function extractMethodData(path: string): EndpointMethodData {
  const [, endpoint, method] = path.split('/');
  return { endpoint, method };
}

export class EndpointMethodProcessor {
  readonly #context: BackbonePluginContext;
  readonly #data: EndpointMethodData;
  readonly #pathItem: EndpointMethodPathItem;

  public constructor(
    [path, pathItem]: EndpointMethodProcessorEntry,
    sources: SourceMap,
    context: BackbonePluginContext
  ) {
    this.#context = context;
    this.#data = extractMethodData(path);
    this.#pathItem = pathItem;
  }

  public process(): void {
    this.#context.logger.info(this.#data, 'Start processing endpoint method');

    for (const method of Object.values(OpenAPIV3.HttpMethods)) {
      if (this.#pathItem[method]) {
        new EndpointMethodOperationProcessor(
          method,
          this.#pathItem[method] as EndpointMethodOperation,
          this.#data,
          this.#context
        ).process();
      }
    }
  }
}
