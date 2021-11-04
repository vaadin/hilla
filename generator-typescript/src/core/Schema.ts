import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';

export function isComposedSchema(schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>): boolean {
  return 'anyOf' in schema;
}

export function isReferenceSchema(
  schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
): schema is ReadonlyDeep<OpenAPIV3.ReferenceObject> {
  return '$ref' in schema;
}

export function isNullableSchema(schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>): boolean {
  return !isReferenceSchema(schema) && !!schema.nullable;
}

export function unwrapSchema(
  schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
): ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject> {
  return 'anyOf' in schema ? schema.anyOf![0] : schema;
}

export type SchemaReferenceDetails = Readonly<{
  identifier: string;
  path: string;
}>;

const COMPONENTS_SCHEMAS_REF = /#\/components\/schemas/;
const QUALIFIED_NAME_DELIMITER = /[$.]/g;

export function getReferenceSchemaDetails(schema: ReadonlyDeep<OpenAPIV3.ReferenceObject>): SchemaReferenceDetails;
export function getReferenceSchemaDetails(schema: ReadonlyDeep<OpenAPIV3.SchemaObject>): undefined;
export function getReferenceSchemaDetails(
  schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
): SchemaReferenceDetails | undefined {
  const _schema = unwrapSchema(schema);

  if (!isReferenceSchema(_schema)) {
    return undefined;
  }

  const { $ref } = _schema;

  return {
    identifier: $ref.substring($ref.lastIndexOf('.'), $ref.length),
    path: `/${$ref.replace(COMPONENTS_SCHEMAS_REF, '').replace(QUALIFIED_NAME_DELIMITER, '/')}`,
  };
}

const positiveCheck = () => true;
const negativeCheck = () => false;

function createSchemaTypeChecker(
  type: OpenAPIV3.ArraySchemaObjectType | OpenAPIV3.NonArraySchemaObjectType,
  checkReferenceSchema: (schema: ReadonlyDeep<OpenAPIV3.ReferenceObject>) => boolean = negativeCheck,
  checkNonReferenceSchema: (schema: ReadonlyDeep<OpenAPIV3.SchemaObject>) => boolean = positiveCheck,
) {
  return (schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>) => {
    const _schema = unwrapSchema(schema);
    const reference = isReferenceSchema(_schema);

    return (
      (reference && type === 'object' && checkReferenceSchema(_schema)) ||
      (!reference && type === _schema.type && checkNonReferenceSchema(_schema))
    );
  };
}

export const isArraySchema = createSchemaTypeChecker('array');

export const isBooleanSchema = createSchemaTypeChecker('boolean');

export const isIntegerSchema = createSchemaTypeChecker('integer');

export const isMapSchema = createSchemaTypeChecker(
  'object',
  negativeCheck,
  (schema) => !schema.properties && !!schema.additionalProperties,
);

export const isNumberSchema = createSchemaTypeChecker('number');

export const isObjectSchema = createSchemaTypeChecker('object', positiveCheck);

export const isStringSchema = createSchemaTypeChecker('string');
