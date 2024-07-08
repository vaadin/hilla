import { isAnyOfRuleComposedSchema, type Schema } from '@vaadin/hilla-generator-core/Schema.js';

export const defaultMediaType = 'application/json';

export type SchemaWithTypeParameters = Readonly<{ 'x-type-parameters': Schema[] }> & Schema;
export type SchemaWithTypeArgument = Readonly<{ 'x-type-argument': string }> & Schema;

export function isSchemaWithTypeParameters(schema: Schema): schema is SchemaWithTypeParameters {
  return 'x-type-parameters' in schema;
}

export function isSchemaWithTypeArgument(schema: Schema): schema is SchemaWithTypeArgument {
  return 'x-type-argument' in schema;
}

export function findTypeParameters(schema: Schema): readonly Schema[] | undefined {
  if (isSchemaWithTypeParameters(schema)) {
    return schema['x-type-parameters'];
  }

  if (isAnyOfRuleComposedSchema(schema)) {
    return schema.anyOf.find(isSchemaWithTypeParameters)?.['x-type-parameters'];
  }

  return undefined;
}

export function findTypeArgument(schema: Schema): string | undefined {
  if (isSchemaWithTypeArgument(schema)) {
    return schema['x-type-argument'];
  }

  return undefined;
}
