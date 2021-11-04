import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { ObjectLiteralExpression, ParameterDeclaration } from 'typescript';
import ts from 'typescript';
import { isObjectSchema } from '../../core/Schema.js';
import SchemaProcessor from './SchemaProcessor.js';
import { createSourceBag, SourceBag, updateSourceBagMutating } from './SourceBag.js';
import type { BackbonePluginContext } from './utils.js';
import { defaultMediaType } from './utils.js';

export type EndpointMethodRequestBody = ReadonlyDeep<OpenAPIV3.RequestBodyObject>;

export type ParameterDeclarationsBag = SourceBag<ParameterDeclaration>;

export type EndpointMethodRequestBodyProcessingResult = Readonly<{
  parameters: ParameterDeclarationsBag;
  packedParameters?: ObjectLiteralExpression;
}>;

export default class EndpointMethodRequestBodyProcessor {
  readonly #context: BackbonePluginContext;
  readonly #requestBody?: EndpointMethodRequestBody;

  public constructor(
    requestBody: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject> | undefined,
    context: BackbonePluginContext,
  ) {
    this.#context = context;
    this.#requestBody = requestBody ? context.resolver.resolve(requestBody) : undefined;
  }

  public process(): EndpointMethodRequestBodyProcessingResult {
    if (!this.#requestBody) {
      return {
        parameters: createSourceBag(),
      };
    }

    const parameterData = this.#extractParameterData(this.#requestBody.content[defaultMediaType]?.schema);

    return {
      parameters: parameterData.reduce<ParameterDeclarationsBag>((acc, [name, schema]) => {
        const { imports, code } = new SchemaProcessor(schema).process();

        return updateSourceBagMutating(
          acc,
          [
            ts.factory.createParameterDeclaration(
              undefined,
              undefined,
              undefined,
              name,
              undefined,
              ts.factory.createUnionTypeNode(code),
            ),
          ],
          imports,
        );
      }, createSourceBag()),
      packedParameters: ts.factory.createObjectLiteralExpression(
        parameterData.map(([name]) => ts.factory.createShorthandPropertyAssignment(name)),
      ),
    };
  }

  #extractParameterData(
    basicSchema?: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
  ): ReadonlyArray<readonly [string, ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>]> {
    if (!basicSchema) {
      return [];
    }

    const { resolver, logger } = this.#context;

    const resolvedSchema = resolver.resolve(basicSchema);

    if (isObjectSchema(resolvedSchema) && resolvedSchema.properties) {
      return Object.entries(resolvedSchema.properties);
    }

    logger.warn("A schema provided for endpoint method's 'requestBody' is not supported");
    return [];
  }
}
