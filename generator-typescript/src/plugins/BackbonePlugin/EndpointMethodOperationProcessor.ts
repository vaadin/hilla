import equal from 'fast-deep-equal';
import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { Expression, Identifier } from 'typescript';
import ts from 'typescript';
import EndpointMethodRequestBodyProcessor from './EndpointMethodRequestBodyProcessor.js';
import EndpointMethodResponseProcessor from './EndpointMethodResponseProcessor.js';
import { createSourceBag, StatementBag, TypeNodesBag, updateSourceBagMutating } from './SourceBag.js';
import type { BackbonePluginContext } from './utils.js';

export type EndpointMethodOperation = ReadonlyDeep<OpenAPIV3.OperationObject>;

export type EndpointMethodOperationProcessorOptions = Readonly<{
  libraryIdentifier: Identifier;
}>;

export default class EndpointMethodOperationProcessor {
  readonly #context: BackbonePluginContext;
  readonly #method: OpenAPIV3.HttpMethods;
  readonly #operation: EndpointMethodOperation;
  readonly #options: EndpointMethodOperationProcessorOptions;

  public constructor(
    method: OpenAPIV3.HttpMethods,
    operation: EndpointMethodOperation,
    options: EndpointMethodOperationProcessorOptions,
    context: BackbonePluginContext,
  ) {
    this.#context = context;
    this.#method = method;
    this.#operation = operation;
    this.#options = options;
  }

  public process(endpointName: string, endpointMethodName: string): StatementBag {
    const { logger } = this.#context;

    switch (this.#method) {
      case OpenAPIV3.HttpMethods.POST:
        logger.info('Processing POST method');
        return this.#processPost(endpointName, endpointMethodName);
      default:
        logger.warn(`Processing ${this.#method.toUpperCase()} currently is not supported`);
        return createSourceBag();
    }
  }

  #processPost(endpointName: string, endpointMethodName: string): StatementBag {
    const { parameters, packedParameters } = new EndpointMethodRequestBodyProcessor(
      this.#operation.requestBody,
      this.#context,
    ).process();

    const clientCallExpression: Array<Expression> = [
      ts.factory.createStringLiteral(endpointName),
      ts.factory.createStringLiteral(endpointMethodName),
    ];

    if (packedParameters) {
      clientCallExpression.push(packedParameters);
    }

    const response = this.#prepareResponse();

    const { libraryIdentifier } = this.#options;
    const uniqueName = ts.factory.createUniqueName(endpointMethodName);

    const declaration = ts.factory.createFunctionDeclaration(
      undefined,
      [ts.factory.createToken(ts.SyntaxKind.AsyncKeyword)],
      undefined,
      uniqueName,
      undefined,
      parameters.code,
      ts.factory.createTypeReferenceNode('Promise', [ts.factory.createUnionTypeNode(response.code)]),
      ts.factory.createBlock([
        ts.factory.createReturnStatement(
          ts.factory.createCallExpression(
            ts.factory.createPropertyAccessExpression(libraryIdentifier, ts.factory.createIdentifier('call')),
            undefined,
            clientCallExpression,
          ),
        ),
      ]),
    );

    return createSourceBag(
      [declaration],
      { ...response.imports, ...parameters.imports },
      { ...response.exports, [endpointMethodName]: uniqueName, ...parameters.exports },
    );
  }

  #prepareResponse(): TypeNodesBag {
    return Object.entries(this.#operation.responses)
      .map(([code, response]) => new EndpointMethodResponseProcessor(code, response, this.#context).process())
      .reduce<TypeNodesBag>(
        (acc, { code, exports, imports }) =>
          updateSourceBagMutating(
            acc,
            code.filter((element) => !acc.code.find((existingElement) => !equal(existingElement, element))),
            imports,
            exports,
          ),
        createSourceBag(),
      );
  }
}
