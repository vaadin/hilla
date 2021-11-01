import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import SchemaProcessor from './SchemaProcessor';
import type { BackbonePluginContext, TypeNodesBag } from './utils';
import { defaultMediaType, emptySourceBag } from './utils';

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
        return emptySourceBag as TypeNodesBag;
    }
  }

  #processOk(): TypeNodesBag {
    const schema = this.#response.content?.[defaultMediaType]?.schema;

    return schema ? new SchemaProcessor(schema).process() : (emptySourceBag as TypeNodesBag);
  }
}
