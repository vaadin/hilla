import { OpenAPIV3 } from 'openapi-types';
import type Pino from 'pino';
import type { ReadonlyDeep } from 'type-fest';
import { factory, Statement, SyntaxKind } from 'typescript';
import type SharedStorage from '../../core/SharedStorage';
import EndpointMethodRequestBodyProcessor from './EndpointMethodRequestBodyProcessor';
import EndpointMethodResponseProcessor from './EndpointMethodResponseProcessor';
import EndpointMethod, { EndpointMethodContext } from './utils';

export type EndpointMethodOperation = ReadonlyDeep<OpenAPIV3.OperationObject>;

export type EndpointMethodOperationProcessorData = Readonly<{
  httpMethod: OpenAPIV3.HttpMethods;
  operation: EndpointMethodOperation;
}>;

export default class EndpointMethodOperationProcessor {
  readonly #context: EndpointMethodContext;
  readonly #logger: Pino.Logger;
  readonly #operation: EndpointMethodOperation;
  readonly #method: OpenAPIV3.HttpMethods;

  public constructor(
    method: OpenAPIV3.HttpMethods,
    operation: EndpointMethodOperation,
    context: EndpointMethodContext,
    logger: Pino.Logger
  ) {
    this.#context = context;
    this.#method = method;
    this.#operation = operation;
    this.#logger = logger;
  }

  public process(): readonly Statement[] {
    switch (this.#method) {
      case OpenAPIV3.HttpMethods.POST:
        this.#logger.info('Processing POST method');
        return this.#processPost();
      default:
        this.#logger.warn(`Processing ${this.#method.toUpperCase()} currently is not supported`);
        return [];
    }
  }

  #processPost(): readonly Statement[] {
    const requestBodyProcessor = new EndpointMethodRequestBodyProcessor(
      this.#operation.requestBody,
      this.#context,
      this.#logger
    );
    const responseProcessor = new EndpointMethodResponseProcessor(this.#operation.responses);

    return [
      factory.createFunctionDeclaration(
        undefined,
        [factory.createToken(SyntaxKind.AsyncKeyword)],
        undefined,
        `_${this.#context.endpointMethod}`,
        undefined,
        requestBodyProcessor.process(),
        responseProcessor.process(),
        factory.createBlock([
          factory.createReturnStatement(
            factory.createCallExpression(
              factory.createPropertyAccessExpression(
                factory.createIdentifier('client'),
                factory.createIdentifier('call')
              ),
              factory.createStringLiteral(this.#context.endpoint),
              factory.createStringLiteral(this.#context.endpointMethod),
              requestBodyProcessor.processClientCallArguments()
            )
          ),
        ])
      ),
    ];
  }
}
