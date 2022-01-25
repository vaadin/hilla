import type Plugin from '@vaadin/generator-typescript-core/Plugin.js';
import {
  isEmptyObject,
  isObjectSchema,
  NonEmptyObjectSchema,
  Schema,
} from '@vaadin/generator-typescript-core/Schema.js';
import type DependencyManager from '@vaadin/generator-typescript-utils/dependencies/DependencyManager.js';
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
}>;

export default class EndpointMethodRequestBodyProcessor {
  readonly #dependencies: DependencyManager;
  readonly #owner: Plugin;
  readonly #requestBody?: EndpointMethodRequestBody;

  public constructor(
    requestBody: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject> | undefined,
    dependencies: DependencyManager,
    owner: Plugin,
  ) {
    this.#owner = owner;
    this.#dependencies = dependencies;
    this.#requestBody = requestBody ? owner.resolver.resolve(requestBody) : undefined;
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

    const { resolver, logger } = this.#owner;

    const resolvedSchema = resolver.resolve(basicSchema);

    if (isObjectSchema(resolvedSchema) && !isEmptyObject(resolvedSchema)) {
      return Object.entries((resolvedSchema as NonEmptyObjectSchema).properties);
    }

    logger.warn("A schema provided for endpoint method's 'requestBody' is not supported");
    return [];
  }
}
