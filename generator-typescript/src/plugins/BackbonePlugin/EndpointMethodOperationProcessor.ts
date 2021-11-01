import equal from 'fast-deep-equal';
import { OpenAPIV3 } from 'openapi-types';
import type { Mutable, ReadonlyDeep } from 'type-fest';
import type { Expression } from 'typescript';
import ts from 'typescript';
import EndpointMethodRequestBodyProcessor from './EndpointMethodRequestBodyProcessor';
import EndpointMethodResponseProcessor from './EndpointMethodResponseProcessor';
import type { BackbonePluginContext, MutableArray, StatementBag, TypeNodesBag } from './utils';
import { emptySourceBag } from './utils';

export type EndpointMethodOperation = ReadonlyDeep<OpenAPIV3.OperationObject>;

export default class EndpointMethodOperationProcessor {
  readonly #context: BackbonePluginContext;
  readonly #operation: EndpointMethodOperation;
  readonly #method: OpenAPIV3.HttpMethods;

  public constructor(
    method: OpenAPIV3.HttpMethods,
    operation: EndpointMethodOperation,
    context: BackbonePluginContext,
  ) {
    this.#context = context;
    this.#method = method;
    this.#operation = operation;
  }

  public process(endpointName: string, endpointMethodName: string): StatementBag {
    const { logger } = this.#context;

    switch (this.#method) {
      case OpenAPIV3.HttpMethods.POST:
        logger.info('Processing POST method');
        return this.#processPost(endpointName, endpointMethodName);
      default:
        logger.warn(`Processing ${this.#method.toUpperCase()} currently is not supported`);
        return emptySourceBag as StatementBag;
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

    const uniqueName = ts.factory.createUniqueName(endpointMethodName);

    const declaration = ts.factory.createFunctionDeclaration(
      undefined,
      [ts.factory.createToken(ts.SyntaxKind.AsyncKeyword)],
      undefined,
      uniqueName,
      undefined,
      parameters.code,
      ts.factory.createUnionTypeNode(response.code),
      ts.factory.createBlock([
        ts.factory.createReturnStatement(
          ts.factory.createCallExpression(
            ts.factory.createPropertyAccessExpression(
              ts.factory.createIdentifier('client'),
              ts.factory.createIdentifier('call'),
            ),
            undefined,
            clientCallExpression,
          ),
        ),
      ]),
    );

    return {
      code: [declaration],
      exports: { ...response.exports, [endpointMethodName]: uniqueName.text, ...parameters.exports },
      imports: { ...response.imports, ...parameters.imports },
    };
  }

  #prepareResponse(): TypeNodesBag {
    return Object.entries(this.#operation.responses)
      .map(([code, response]) => new EndpointMethodResponseProcessor(code, response, this.#context).process())
      .reduce<TypeNodesBag>(
        (acc, { code, exports, imports }) => {
          if (!acc.code.some((element) => equal(element, code))) {
            (acc.code as MutableArray<TypeNodesBag['code']>).push(...code);
          }

          (acc as Mutable<TypeNodesBag>).exports = Object.assign(acc.exports, exports);
          (acc as Mutable<TypeNodesBag>).imports = Object.assign(acc.imports, imports);

          return acc;
        },
        {
          code: [],
        },
      );
  }
}
