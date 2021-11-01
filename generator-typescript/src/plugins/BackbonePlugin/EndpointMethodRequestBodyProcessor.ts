import type { OpenAPIV3 } from 'openapi-types';
import type { Mutable, ReadonlyDeep } from 'type-fest';
import ts from 'typescript';
import type { ObjectLiteralExpression, ParameterDeclaration } from 'typescript';
import { isObjectSchema } from '../../core/Schema';
import SchemaProcessor from './SchemaProcessor';
import type { BackbonePluginContext, MutableArray, SourceBag } from './utils';
import { defaultMediaType, emptySourceBag } from './utils';

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
        parameters: emptySourceBag as ParameterDeclarationsBag,
      };
    }

    const parameterData = this.#extractParameterData(this.#requestBody.content[defaultMediaType]?.schema);

    return {
      parameters: parameterData.reduce<ParameterDeclarationsBag>(
        (acc, [name, schema]) => {
          const { imports, code } = new SchemaProcessor(schema).process();

          const parameterDeclaration = ts.factory.createParameterDeclaration(
            undefined,
            undefined,
            undefined,
            name,
            undefined,
            ts.factory.createUnionTypeNode(code),
          );

          (acc as Mutable<ParameterDeclarationsBag>).imports = Object.assign(
            acc.imports as Mutable<ParameterDeclarationsBag['imports']>,
            imports,
          );
          (acc.code as MutableArray<ParameterDeclarationsBag['code']>).push(parameterDeclaration);

          return acc;
        },
        {
          code: [],
        },
      ),
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
