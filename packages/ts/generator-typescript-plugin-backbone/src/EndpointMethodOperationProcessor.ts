import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import ClientPlugin from '@hilla/generator-typescript-plugin-client';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import equal from 'fast-deep-equal';
import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { Expression, Statement, TypeNode } from 'typescript';
import ts from 'typescript';
import EndpointMethodRequestBodyProcessor from './EndpointMethodRequestBodyProcessor.js';
import EndpointMethodResponseProcessor from './EndpointMethodResponseProcessor.js';

export type EndpointMethodOperation = ReadonlyDeep<OpenAPIV3.OperationObject>;

export const INIT_TYPE_NAME = 'EndpointRequestInit';
export const HILLA_FRONTEND_NAME = '@hilla/frontend';

export default abstract class EndpointMethodOperationProcessor {
  public static createProcessor(
    httpMethod: OpenAPIV3.HttpMethods,
    endpointName: string,
    endpointMethodName: string,
    operation: EndpointMethodOperation,
    dependencies: DependencyManager,
    owner: Plugin,
  ): EndpointMethodOperationProcessor | undefined {
    switch (httpMethod) {
      case OpenAPIV3.HttpMethods.POST:
        // eslint-disable-next-line no-use-before-define
        return new EndpointMethodOperationPOSTProcessor(
          endpointName,
          endpointMethodName,
          operation,
          dependencies,
          owner,
        );
      default:
        owner.logger.warn(`Processing ${httpMethod.toUpperCase()} currently is not supported`);
        return undefined;
    }
  }

  public abstract process(outputDir?: string): Promise<Statement | undefined>;
}

class EndpointMethodOperationPOSTProcessor extends EndpointMethodOperationProcessor {
  readonly #dependencies: DependencyManager;
  readonly #endpointMethodName: string;
  readonly #endpointName: string;
  readonly #operation: EndpointMethodOperation;
  readonly #owner: Plugin;

  public constructor(
    endpointName: string,
    endpointMethodName: string,
    operation: EndpointMethodOperation,
    dependencies: DependencyManager,
    owner: Plugin,
  ) {
    super();
    this.#owner = owner;
    this.#dependencies = dependencies;
    this.#endpointName = endpointName;
    this.#endpointMethodName = endpointMethodName;
    this.#operation = operation;
  }

  public async process(outputDir?: string): Promise<Statement | undefined> {
    const { exports, imports, paths } = this.#dependencies;
    this.#owner.logger.debug(`${this.#endpointName}.${this.#endpointMethodName} - processing POST method`);
    const initTypeIdentifier = imports.named.getIdentifier(
      paths.createBareModulePath(HILLA_FRONTEND_NAME),
      INIT_TYPE_NAME,
    )!;

    const { parameters, packedParameters, initParam } = new EndpointMethodRequestBodyProcessor(
      this.#operation.requestBody,
      this.#dependencies,
      this.#owner,
      initTypeIdentifier,
    ).process();

    const methodIdentifier = exports.named.add(this.#endpointMethodName);
    const clientLibIdentifier = imports.default.getIdentifier(
      paths.createRelativePath(await ClientPlugin.getClientFileName(outputDir)),
    )!;

    const callExpression = ts.factory.createCallExpression(
      ts.factory.createPropertyAccessExpression(clientLibIdentifier, ts.factory.createIdentifier('call')),
      undefined,
      [
        ts.factory.createStringLiteral(this.#endpointName),
        ts.factory.createStringLiteral(this.#endpointMethodName),
        packedParameters,
        initParam,
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
      ts.factory.createBlock([ts.factory.createReturnStatement(callExpression)]),
    );
  }

  #prepareResponseType(): TypeNode {
    this.#owner.logger.debug(`${this.#endpointName}.${this.#endpointMethodName} POST - processing response type`);

    const responseTypes = Object.entries(this.#operation.responses)
      .flatMap(([code, response]) =>
        new EndpointMethodResponseProcessor(code, response, this.#dependencies, this.#owner).process(),
      )
      .filter((value, index, arr) => arr.findIndex((v) => equal(v, value)) === index);

    if (responseTypes.length === 0) {
      return ts.factory.createKeywordTypeNode(ts.SyntaxKind.VoidKeyword);
    }

    return ts.factory.createUnionTypeNode(responseTypes);
  }
}
