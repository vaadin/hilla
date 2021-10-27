import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import { factory, Statement, SyntaxKind } from 'typescript';
import EndpointMethodRequestBodyProcessor from './EndpointMethodRequestBodyProcessor';
import EndpointMethodResponseProcessor from './EndpointMethodResponseProcessor';
import type { BackbonePluginContext, EndpointMethodData } from './utils';

export type EndpointMethodOperation = ReadonlyDeep<OpenAPIV3.OperationObject>;

export default class EndpointMethodOperationProcessor {
  readonly #context: BackbonePluginContext;
  readonly #endpointMethodData: EndpointMethodData;
  readonly #operation: EndpointMethodOperation;
  readonly #method: OpenAPIV3.HttpMethods;

  public constructor(
    method: OpenAPIV3.HttpMethods,
    operation: EndpointMethodOperation,
    endpointMethodData: EndpointMethodData,
    context: BackbonePluginContext
  ) {
    this.#context = context;
    this.#endpointMethodData = endpointMethodData;
    this.#method = method;
    this.#operation = operation;
  }

  public process(): readonly Statement[] {
    const { logger } = this.#context;

    switch (this.#method) {
      case OpenAPIV3.HttpMethods.POST:
        logger.info('Processing POST method');
        return this.#processPost();
      default:
        logger.warn(`Processing ${this.#method.toUpperCase()} currently is not supported`);
        return [];
    }
  }

  #processPost(): readonly Statement[] {
    const { endpoint, method } = this.#endpointMethodData;

    const requestBodyProcessor = new EndpointMethodRequestBodyProcessor(this.#operation.requestBody, this.#context);
    const responseProcessor = new EndpointMethodResponseProcessor(this.#operation.responses, this.#context);

    return [
      factory.createFunctionDeclaration(
        undefined,
        [factory.createToken(SyntaxKind.AsyncKeyword)],
        undefined,
        `_${method}`,
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
              factory.createStringLiteral(endpoint),
              factory.createStringLiteral(method),
              requestBodyProcessor.processClientCallArguments()
            )
          ),
        ])
      ),
    ];
  }
}
