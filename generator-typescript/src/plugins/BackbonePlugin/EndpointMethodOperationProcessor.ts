import equal from 'fast-deep-equal';
import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { CallExpression, Expression, Statement, TypeNode } from 'typescript';
import ts from 'typescript';
import type DependencyManager from './DependencyManager.js';
import EndpointMethodRequestBodyProcessor from './EndpointMethodRequestBodyProcessor.js';
import EndpointMethodResponseProcessor from './EndpointMethodResponseProcessor.js';
import type { BackbonePluginContext } from './utils.js';
import { clientLib } from './utils.js';

export type EndpointMethodOperation = ReadonlyDeep<OpenAPIV3.OperationObject>;

function wrapCallExpression(callExpression: CallExpression, responseType: TypeNode): Statement {
  if (ts.isUnionTypeNode(responseType)) {
    return ts.factory.createReturnStatement(callExpression);
  }

  return ts.factory.createExpressionStatement(callExpression);
}

class EndpointPOSTMethodProcessor {
  readonly #context: BackbonePluginContext;
  readonly #dependencies: DependencyManager;
  readonly #operation: EndpointMethodOperation;

  public constructor(
    operation: EndpointMethodOperation,
    dependencies: DependencyManager,
    context: BackbonePluginContext,
  ) {
    this.#dependencies = dependencies;
    this.#context = context;
    this.#operation = operation;
  }

  public process(endpointName: string, endpointMethodName: string): Statement | undefined {
    this.#context.logger.info('Processing POST method');

    const { parameters, packedParameters } = new EndpointMethodRequestBodyProcessor(
      this.#operation.requestBody,
      this.#dependencies,
      this.#context,
    ).process();

    const methodIdentifier = this.#dependencies.exports.register(endpointMethodName);
    const clientLibIdentifier = this.#dependencies.imports.getIdentifier(clientLib.specifier, clientLib.path)!;

    const callExpression = ts.factory.createCallExpression(
      ts.factory.createPropertyAccessExpression(clientLibIdentifier, ts.factory.createIdentifier('call')),
      undefined,
      [
        ts.factory.createStringLiteral(endpointName),
        ts.factory.createStringLiteral(endpointMethodName),
        packedParameters,
      ].filter(Boolean) as readonly Expression[],
    );

    const responseType = this.#prepareResponseType();

    return ts.factory.createFunctionDeclaration(
      undefined,
      [ts.factory.createToken(ts.SyntaxKind.AsyncKeyword)],
      undefined,
      methodIdentifier,
      undefined,
      parameters,
      ts.factory.createTypeReferenceNode('Promise', [responseType]),
      ts.factory.createBlock([wrapCallExpression(callExpression, responseType)]),
    );
  }

  #prepareResponseType(): TypeNode {
    const responseTypes = Object.entries(this.#operation.responses)
      .flatMap(([code, response]) =>
        new EndpointMethodResponseProcessor(code, response, this.#dependencies, this.#context).process(),
      )
      .filter((value, index, arr) => arr.findIndex((v) => equal(v, value)) === index);

    if (responseTypes.length === 0) {
      return ts.factory.createKeywordTypeNode(ts.SyntaxKind.VoidKeyword);
    }

    return ts.factory.createUnionTypeNode(responseTypes);
  }
}

export default class EndpointMethodOperationProcessor {
  readonly #context: BackbonePluginContext;
  readonly #dependencies: DependencyManager;
  readonly #method: OpenAPIV3.HttpMethods;
  readonly #operation: EndpointMethodOperation;

  public constructor(
    method: OpenAPIV3.HttpMethods,
    operation: EndpointMethodOperation,
    dependencies: DependencyManager,
    context: BackbonePluginContext,
  ) {
    this.#context = context;
    this.#dependencies = dependencies;
    this.#method = method;
    this.#operation = operation;
  }

  public process(endpointName: string, endpointMethodName: string): Statement | undefined {
    switch (this.#method) {
      case OpenAPIV3.HttpMethods.POST:
        return new EndpointPOSTMethodProcessor(this.#operation, this.#dependencies, this.#context).process(
          endpointName,
          endpointMethodName,
        );
      default:
        this.#context.logger.warn(`Processing ${this.#method.toUpperCase()} currently is not supported`);
        return undefined;
    }
  }
}
