import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import { factory } from 'typescript';
import type { TypeNode } from 'typescript';
import Schema from '../../core/Schema';
import SchemaProcessor from './SchemaProcessor';
import type { BackbonePluginContext } from './utils';

export type EndpointMethodResponses = ReadonlyDeep<OpenAPIV3.ResponsesObject>;
export type EndpointMethodResponse = ReadonlyDeep<OpenAPIV3.ResponseObject>;

const defaultMediaType = 'application/json';

class EndpointMethodResponseProcessor {
  readonly #code: string;
  readonly #context: BackbonePluginContext;
  readonly #response: EndpointMethodResponse;

  public constructor(code: string, response: EndpointMethodResponses[string], context: BackbonePluginContext) {
    this.#code = code;
    this.#context = context;
    this.#response = context.resolver.resolve(response);
  }

  public process(): TypeNode | undefined {
    switch (this.#code) {
      case '200':
        return this.#processOk();
      default:
        this.#context.logger.warn(`Response code '${this.#code} is not supported'`);
        return undefined;
    }
  }

  #processOk(): TypeNode | undefined {
    const schema = this.#response.content?.[defaultMediaType]?.schema;

    if (!schema) {
      return undefined;
    }

    return new SchemaProcessor(Schema.of(schema), this.#context).process();
  }
}

export default class EndpointMethodResponsesProcessor {
  readonly #context: BackbonePluginContext;
  readonly #responses: ReadonlyDeep<OpenAPIV3.ResponsesObject>;

  public constructor(responses: EndpointMethodResponses, context: BackbonePluginContext) {
    this.#responses = responses;
    this.#context = context;
  }

  public process(): TypeNode | undefined {
    const types = Object.entries(this.#responses)
      .map(([code, response]) => new EndpointMethodResponseProcessor(code, response, this.#context).process())
      .filter(Boolean) as TypeNode[];

    return types.length > 1 ? factory.createUnionTypeNode(types) : types[0];
  }
}
