import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import {
  isEmptyObject,
  isObjectSchema,
  type NonEmptyObjectSchema,
  type Schema,
} from '@hilla/generator-typescript-core/Schema.js';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import ts, { type ObjectLiteralExpression, type ParameterDeclaration } from 'typescript';
import TypeSchemaProcessor from './TypeSchemaProcessor.js';
import { defaultMediaType } from './utils.js';

export type EndpointMethodRequestBody = ReadonlyDeep<OpenAPIV3.RequestBodyObject>;

export type EndpointMethodRequestBodyProcessingResult = Readonly<{
  parameters: readonly ParameterDeclaration[];
  packedParameters?: ObjectLiteralExpression;
  initParam: ts.Identifier;
}>;

export default class EndpointMethodRequestBodyProcessor {
  static readonly #defaultInitParamName = 'init';

  readonly #dependencies: DependencyManager;
  readonly #owner: Plugin;
  readonly #requestBody?: EndpointMethodRequestBody;
  readonly #initTypeIdentifier: ts.Identifier;

  constructor(
    requestBody: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject> | undefined,
    dependencies: DependencyManager,
    owner: Plugin,
    initTypeIdentifier: ts.Identifier,
  ) {
    this.#owner = owner;
    this.#dependencies = dependencies;
    this.#requestBody = requestBody ? owner.resolver.resolve(requestBody) : undefined;
    this.#initTypeIdentifier = initTypeIdentifier;
  }

  process(): EndpointMethodRequestBodyProcessingResult {
    if (!this.#requestBody) {
      return {
        initParam: ts.factory.createIdentifier(EndpointMethodRequestBodyProcessor.#defaultInitParamName),
        packedParameters: ts.factory.createObjectLiteralExpression(),
        parameters: [
          ts.factory.createParameterDeclaration(
            undefined,
            undefined,
            EndpointMethodRequestBodyProcessor.#defaultInitParamName,
            ts.factory.createToken(ts.SyntaxKind.QuestionToken),
            ts.factory.createTypeReferenceNode(this.#initTypeIdentifier),
          ),
        ],
      };
    }

    const parameterData = this.#extractParameterData(this.#requestBody.content[defaultMediaType].schema);
    const parameterNames = parameterData.map(([name]) => name);
    let initParamName = EndpointMethodRequestBodyProcessor.#defaultInitParamName;

    while (parameterNames.includes(initParamName)) {
      initParamName = `_${initParamName}`;
    }

    return {
      initParam: ts.factory.createIdentifier(initParamName),
      packedParameters: ts.factory.createObjectLiteralExpression(
        parameterData.map(([name]) => ts.factory.createShorthandPropertyAssignment(name)),
      ),
      parameters: [
        ...parameterData.map(([name, schema]) => {
          const nodes = new TypeSchemaProcessor(schema, this.#dependencies).process();

          return ts.factory.createParameterDeclaration(
            undefined,
            undefined,
            name,
            undefined,
            ts.factory.createUnionTypeNode(nodes),
          );
        }),
        ts.factory.createParameterDeclaration(
          undefined,
          undefined,
          initParamName,
          ts.factory.createToken(ts.SyntaxKind.QuestionToken),
          ts.factory.createTypeReferenceNode(this.#initTypeIdentifier),
        ),
      ],
    };
  }

  #extractParameterData(
    basicSchema?: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
  ): Array<readonly [string, Schema]> {
    if (!basicSchema) {
      return [];
    }

    const { logger, resolver } = this.#owner;

    const resolvedSchema = resolver.resolve(basicSchema);

    if (isObjectSchema(resolvedSchema) && !isEmptyObject(resolvedSchema)) {
      return Object.entries((resolvedSchema as NonEmptyObjectSchema).properties);
    }

    logger.warn("A schema provided for endpoint method's 'requestBody' is not supported");
    return [];
  }
}
