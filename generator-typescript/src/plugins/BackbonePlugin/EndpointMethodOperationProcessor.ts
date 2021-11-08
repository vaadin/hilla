import equal from 'fast-deep-equal';
import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { Expression, Statement, TypeNode } from 'typescript';
import ts from 'typescript';
import type DependencyManager from './DependencyManager.js';
import EndpointMethodRequestBodyProcessor from './EndpointMethodRequestBodyProcessor.js';
import EndpointMethodResponseProcessor from './EndpointMethodResponseProcessor.js';
import type { BackbonePluginContext } from './utils.js';
import { clientLib } from './utils.js';

export type EndpointMethodOperation = ReadonlyDeep<OpenAPIV3.OperationObject>;

export default class EndpointMethodOperationProcessor {
  readonly #context: BackbonePluginContext;
  readonly #dependencies: DependencyManager;
  readonly #method: OpenAPIV3.HttpMethods;
  readonly #operation: EndpointMethodOperation;

  public constructor(
    method: OpenAPIV3.HttpMethods,
    operation: EndpointMethodOperation,
    dependencyManager: DependencyManager,
    context: BackbonePluginContext,
  ) {
    this.#context = context;
    this.#dependencies = dependencyManager;
    this.#method = method;
    this.#operation = operation;
  }

  public process(endpointName: string, endpointMethodName: string): Statement | undefined {
    const { logger } = this.#context;

    switch (this.#method) {
      case OpenAPIV3.HttpMethods.POST:
        logger.info('Processing POST method');
        return this.#processPost(endpointName, endpointMethodName);
      default:
        logger.warn(`Processing ${this.#method.toUpperCase()} currently is not supported`);
        return undefined;
    }
  }

  #prepareResponseType(): readonly TypeNode[] {
    return Object.entries(this.#operation.responses)
      .flatMap(([code, response]) =>
        new EndpointMethodResponseProcessor(code, response, this.#dependencies, this.#context).process(),
      )
      .filter((value, index, arr) => arr.findIndex((v) => equal(v, value)) === index);
  }

  #processPost(endpointName: string, endpointMethodName: string): Statement {
    const { parameters, packedParameters } = new EndpointMethodRequestBodyProcessor(
      this.#operation.requestBody,
      this.#dependencies,
      this.#context,
    ).process();

    const clientCallExpression: Array<Expression> = [
      ts.factory.createStringLiteral(endpointName),
      ts.factory.createStringLiteral(endpointMethodName),
    ];

    if (packedParameters) {
      clientCallExpression.push(packedParameters);
    }

    const responseType = this.#prepareResponseType();

    const methodIdentifier = this.#dependencies.exports.register(endpointMethodName);
    const clientLibIdentifier = this.#dependencies.imports.getIdentifier(clientLib.specifier, clientLib.path)!;

    return ts.factory.createFunctionDeclaration(
      undefined,
      [ts.factory.createToken(ts.SyntaxKind.AsyncKeyword)],
      undefined,
      methodIdentifier,
      undefined,
      parameters,
      ts.factory.createTypeReferenceNode('Promise', [ts.factory.createUnionTypeNode(responseType)]),
      ts.factory.createBlock([
        ts.factory.createReturnStatement(
          ts.factory.createCallExpression(
            ts.factory.createPropertyAccessExpression(clientLibIdentifier, ts.factory.createIdentifier('call')),
            undefined,
            clientCallExpression,
          ),
        ),
      ]),
    );
  }
}
