import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { ParameterDeclaration } from 'typescript';
import type { BackbonePluginContext, EndpointMethodData } from './utils';

export type EndpointMethodRequestBody = ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject>;

export default class EndpointMethodRequestBodyProcessor {
  readonly #context: BackbonePluginContext;
  readonly #requestBody?: EndpointMethodRequestBody;

  public constructor(requestBody: EndpointMethodRequestBody | undefined, context: BackbonePluginContext) {
    this.#context = context;
    this.#requestBody = requestBody;
  }

  public process(): readonly ParameterDeclaration[] {
    if (!this.#requestBody) {
      return [];
    }
  }

  public processClientCallArguments
}
