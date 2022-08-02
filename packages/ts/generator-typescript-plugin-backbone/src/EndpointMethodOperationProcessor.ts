import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import ClientPlugin from '@hilla/generator-typescript-plugin-client';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import equal from 'fast-deep-equal';
import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { CallExpression, Expression, Statement, TypeNode } from 'typescript';
import ts from 'typescript';
import EndpointMethodRequestBodyProcessor from './EndpointMethodRequestBodyProcessor.js';
import EndpointMethodResponseProcessor from './EndpointMethodResponseProcessor.js';
import { defaultMediaType, xClassName } from './utils.js';

export type EndpointMethodOperation = ReadonlyDeep<OpenAPIV3.OperationObject>;

export const INIT_TYPE_NAME = 'EndpointRequestInit';
export const SUBSCRIPTION_TYPE_NAME = 'Subscription';
export const HILLA_FRONTEND_NAME = '@hilla/frontend';

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

  public abstract process(): Statement | undefined;
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

  public process(): Statement | undefined {
    const { exports, imports, paths } = this.#dependencies;
    this.#owner.logger.debug(`${this.#endpointName}.${this.#endpointMethodName} - processing POST method`);
    const initTypeIdentifier = imports.named.getIdentifier(
      paths.createBareModulePath(HILLA_FRONTEND_NAME),
      INIT_TYPE_NAME,
    )!;
    const subscriptionTypeIdentifier = imports.named.getIdentifier(
      paths.createBareModulePath(HILLA_FRONTEND_NAME),
      SUBSCRIPTION_TYPE_NAME,
    )!;

    const pushOperation = Object.entries(this.#operation.responses)
      .map(([_code, response]) => {
        const content = this.#owner.resolver.resolve(response).content?.[defaultMediaType];
        // @ts-ignore
        return content?.schema?.[xClassName] as string | undefined;
      })
      .filter(Boolean)
      .map((x) => x!)
      .some((x) => ['reactor.core.publisher.Flux', 'dev.hilla.EndpointSubscription'].includes(x));

    const responseType = this.#prepareResponseType();

    // In case of push operations, the endpoint method call must be altered heavily.
    // All modified values are redefined here
    const [optionalInitTypeIdentifier, callMethod, patchResponseType, functionType] = pushOperation
      ? [
          undefined, // 'init' is suppressed
          'subscribe', // 'client.subscribe' is called instead of 'client.call'
          (rt: TypeNode) => {
            // @ts-ignore
            rt.types[0].typeName = subscriptionTypeIdentifier;
          }, // response is patched to replace 'Array' with 'Subscription'
          responseType, // type is returned directly without a 'Promise'
        ]
      : [
          initTypeIdentifier,
          'call',
          (rt: TypeNode) => rt,
          ts.factory.createTypeReferenceNode('Promise', [responseType]),
        ];

    patchResponseType(responseType);

    const { parameters, packedParameters, initParam } = new EndpointMethodRequestBodyProcessor(
      this.#operation.requestBody,
      this.#dependencies,
      this.#owner,
      optionalInitTypeIdentifier,
    ).process();

    const methodIdentifier = exports.named.add(this.#endpointMethodName);
    const clientLibIdentifier = imports.default.getIdentifier(paths.createRelativePath(ClientPlugin.CLIENT_FILE_NAME))!;

    const callExpression = ts.factory.createCallExpression(
      ts.factory.createPropertyAccessExpression(clientLibIdentifier, ts.factory.createIdentifier(callMethod)),
      undefined,
      [
        ts.factory.createStringLiteral(this.#endpointName),
        ts.factory.createStringLiteral(this.#endpointMethodName),
        packedParameters,
        initParam,
      ].filter(Boolean) as readonly Expression[],
    );

    return ts.factory.createFunctionDeclaration(
      undefined,
      undefined,
      undefined,
      methodIdentifier,
      undefined,
      parameters,
      functionType,
      ts.factory.createBlock([wrapCallExpression(callExpression, responseType)]),
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
