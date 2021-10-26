import type { OpenAPIV3 } from 'openapi-types';
import type Pino from 'pino';
import type { ReadonlyDeep } from 'type-fest';
import type { TypeNode } from 'typescript';

export type EndpointMethodResponse = ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.ResponseObject>;

export type EndpointMethodResponseProcessorEntry = readonly [code: string, response: EndpointMethodResponse];

export default class EndpointMethodResponseProcessor {
  readonly #responseCode: string;
  readonly #response: ReadonlyDeep<OpenAPIV3.ResponseObject>;
  readonly #logger: Pino.Logger;

  public constructor([code, response]: EndpointMethodResponseProcessorEntry, logger: Pino.Logger) {
    this.#logger = logger;
  }

  public process(): TypeNode | undefined {
    const { responseCode } = this.#data;

    switch (responseCode) {
      case '200':
        return this.#processOk();
      default:
        this.#logger.warn(`Response code '${responseCode} is not supported'`);
        return undefined;
    }
  }

  #processOk(): TypeNode | undefined {

  }
}
