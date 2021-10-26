import type { OpenAPIV3 } from 'openapi-types';
import type Pino from 'pino';
import type { ReadonlyDeep } from 'type-fest';
import type { ParameterDeclaration } from 'typescript';

export type EndpointMethodRequestBody = ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject>;

export default class EndpointMethodRequestBodyProcessor {
  readonly #requestBody?: EndpointMethodRequestBody;

  public constructor(requestBody?: EndpointMethodRequestBody, logger: Pino.Logger) {
    super(logger);

    this.#requestBody = requestBody;
  }

  public process(): readonly ParameterDeclaration[] {
    if (!this.#requestBody) {
      return [];
    }
  }

  public processClientCallArguments
}
