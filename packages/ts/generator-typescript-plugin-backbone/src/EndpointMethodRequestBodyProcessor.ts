import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import {
  isEmptyObject,
  isObjectSchema,
  NonEmptyObjectSchema,
  Schema,
} from '@hilla/generator-typescript-core/Schema.js';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { ObjectLiteralExpression, ParameterDeclaration } from 'typescript';
import ts from 'typescript';
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

  public constructor(
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

  public process(): EndpointMethodRequestBodyProcessingResult {
    if (!this.#requestBody) {
      return {
        parameters: [
          ts.factory.createParameterDeclaration(
            undefined,
            undefined,
            undefined,
            EndpointMethodRequestBodyProcessor.#defaultInitParamName,
            ts.factory.createToken(ts.SyntaxKind.QuestionToken),
            ts.factory.createTypeReferenceNode(this.#initTypeIdentifier),
          ),
        ],
        packedParameters: ts.factory.createObjectLiteralExpression(),
        initParam: ts.factory.createIdentifier(EndpointMethodRequestBodyProcessor.#defaultInitParamName),
      };
    }

    const parameterData = this.#extractParameterData(this.#requestBody.content[defaultMediaType]?.schema);
    const parameterNames = parameterData.map(([name]) => name);
    let initParamName = EndpointMethodRequestBodyProcessor.#defaultInitParamName;

    while (parameterNames.includes(initParamName)) {
      initParamName = `_${initParamName}`;
    }

    return {
      parameters: [
        ...parameterData.map(([name, schema]) => {
          const nodes = new TypeSchemaProcessor(schema, this.#dependencies).process();

          return ts.factory.createParameterDeclaration(
            undefined,
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
          undefined,
          initParamName,
          ts.factory.createToken(ts.SyntaxKind.QuestionToken),
          ts.factory.createTypeReferenceNode(this.#initTypeIdentifier),
        ),
      ],
      packedParameters: ts.factory.createObjectLiteralExpression(
        parameterData.map(([name]) => ts.factory.createShorthandPropertyAssignment(name)),
      ),
      initParam: ts.factory.createIdentifier(initParamName),
    };
  }

  #extractParameterData(
    basicSchema?: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
  ): Array<readonly [string, Schema]> {
    if (!basicSchema) {
      return [];
    }

    const { resolver, logger } = this.#owner;

    const resolvedSchema = resolver.resolve(basicSchema);

    if (isObjectSchema(resolvedSchema) && !isEmptyObject(resolvedSchema)) {
      return Object.entries((resolvedSchema as NonEmptyObjectSchema).properties);
    }

    logger.warn("A schema provided for endpoint method's 'requestBody' is not supported");
    return [];
  }
}
