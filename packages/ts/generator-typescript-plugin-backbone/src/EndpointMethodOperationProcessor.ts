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

export default abstract class EndpointMethodOperationProcessor {
  public static createProcessor(
    httpMethod: OpenAPIV3.HttpMethods,
    endpointName: string,
    endpointMethodName: string,
    operation: EndpointMethodOperation,
    dependencies: DependencyManager,
    context: BackbonePluginContext,
  ): EndpointMethodOperationProcessor | undefined {
    switch (httpMethod) {
      case OpenAPIV3.HttpMethods.POST:
        // eslint-disable-next-line no-use-before-define
        return new EndpointMethodOperationPOSTProcessor(
          endpointName,
          endpointMethodName,
          operation,
          dependencies,
          context,
        );
      default:
        context.logger.warn(`Processing ${httpMethod.toUpperCase()} currently is not supported`);
        return undefined;
    }
  }

  public abstract process(): Statement | undefined;
}

class EndpointMethodOperationPOSTProcessor extends EndpointMethodOperationProcessor {
  readonly #context: BackbonePluginContext;
  readonly #dependencies: DependencyManager;
  readonly #endpointName: string;
  readonly #endpointMethodName: string;
  readonly #operation: EndpointMethodOperation;

  public constructor(
    endpointName: string,
    endpointMethodName: string,
    operation: EndpointMethodOperation,
    dependencies: DependencyManager,
    context: BackbonePluginContext,
  ) {
    super();
    this.#context = context;
    this.#dependencies = dependencies;
    this.#endpointName = endpointName;
    this.#endpointMethodName = endpointMethodName;
    this.#operation = operation;
  }

  public process(): Statement | undefined {
    this.#context.logger.debug(`${this.#endpointName}.${this.#endpointMethodName} — processing POST method`);

    const { parameters, packedParameters } = new EndpointMethodRequestBodyProcessor(
      this.#operation.requestBody,
      this.#dependencies,
      this.#context,
    ).process();

    const methodIdentifier = this.#dependencies.exports.register(this.#endpointMethodName);
    const clientLibIdentifier = this.#dependencies.imports.getIdentifier(clientLib.specifier, clientLib.path)!;

    const callExpression = ts.factory.createCallExpression(
      ts.factory.createPropertyAccessExpression(clientLibIdentifier, ts.factory.createIdentifier('call')),
      undefined,
      [
        ts.factory.createStringLiteral(this.#endpointName),
        ts.factory.createStringLiteral(this.#endpointMethodName),
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
    this.#context.logger.debug(`${this.#endpointName}.${this.#endpointMethodName} POST — processing response type`);

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
