import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { ObjectLiteralExpression, ParameterDeclaration } from 'typescript';
import ts from 'typescript';
import Schema from '../../core/Schema.js';
import type DependencyManager from './DependencyManager.js';
import SchemaProcessor from './SchemaProcessor.js';
import type { BackbonePluginContext } from './utils.js';
import { defaultMediaType } from './utils.js';

export type EndpointMethodRequestBody = ReadonlyDeep<OpenAPIV3.RequestBodyObject>;

export type EndpointMethodRequestBodyProcessingResult = Readonly<{
  parameters: readonly ParameterDeclaration[];
  packedParameters?: ObjectLiteralExpression;
}>;

export default class EndpointMethodRequestBodyProcessor {
  readonly #context: BackbonePluginContext;
  readonly #dependencies: DependencyManager;
  readonly #requestBody?: EndpointMethodRequestBody;

  public constructor(
    requestBody: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject> | undefined,
    dependencies: DependencyManager,
    context: BackbonePluginContext,
  ) {
    this.#context = context;
    this.#dependencies = dependencies;
    this.#requestBody = requestBody ? context.resolver.resolve(requestBody) : undefined;
  }

  public process(): EndpointMethodRequestBodyProcessingResult {
    if (!this.#requestBody) {
      return {
        parameters: [],
      };
    }

    const parameterData = this.#extractParameterData(this.#requestBody.content[defaultMediaType]?.schema);

    return {
      parameters: parameterData.map(([name, schema]) => {
        const nodes = new SchemaProcessor(schema, this.#dependencies).process();

        return ts.factory.createParameterDeclaration(
          undefined,
          undefined,
          undefined,
          name,
          undefined,
          ts.factory.createUnionTypeNode(nodes),
        );
      }),
      packedParameters: ts.factory.createObjectLiteralExpression(
        parameterData.map(([name]) => ts.factory.createShorthandPropertyAssignment(name)),
      ),
    };
  }

  #extractParameterData(
    basicSchema?: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
  ): ReadonlyArray<readonly [string, Schema]> {
    if (!basicSchema) {
      return [];
    }

    const { resolver, logger } = this.#context;

    const resolvedSchema = Schema.of(resolver.resolve(basicSchema));

    if (resolvedSchema.isObject() && !resolvedSchema.isEmptyObject()) {
      const { properties } = resolvedSchema;

      return properties ? [...properties] : [];
    }

    logger.warn("A schema provided for endpoint method's 'requestBody' is not supported");
    return [];
  }
}
