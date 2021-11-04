import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import Schema from '../../core/Schema.js';
import SchemaProcessor from './SchemaProcessor.js';
import { createSourceBag, TypeNodesBag } from './SourceBag.js';
import type { BackbonePluginContext } from './utils.js';
import { defaultMediaType } from './utils.js';

export type EndpointMethodResponses = ReadonlyDeep<OpenAPIV3.ResponsesObject>;
export type EndpointMethodResponse = ReadonlyDeep<OpenAPIV3.ResponseObject>;

export default class EndpointMethodResponseProcessor {
  readonly #code: string;
  readonly #context: BackbonePluginContext;
  readonly #response: EndpointMethodResponse;

  public constructor(code: string, response: EndpointMethodResponses[string], context: BackbonePluginContext) {
    this.#code = code;
    this.#context = context;
    this.#response = context.resolver.resolve(response);
  }

  public process(): TypeNodesBag {
    switch (this.#code) {
      case '200':
        return this.#processOk();
      default:
        this.#context.logger.warn(`Response code '${this.#code} is not supported'`);
        return createSourceBag();
    }
  }

  #processOk(): TypeNodesBag {
    const rawSchema = this.#response.content?.[defaultMediaType]?.schema;

    return rawSchema ? new SchemaProcessor(Schema.of(rawSchema)).process() : createSourceBag();
  }
}
