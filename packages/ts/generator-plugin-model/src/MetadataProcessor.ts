import ts, { type ObjectLiteralExpression, type PropertyAssignment } from '@typescript/typescript6';
import type { Schema } from '@vaadin/hilla-generator-core/Schema.js';
import { createExpressionFromValue } from './utils.js';

interface Annotation {
  name: string;
  attributes?: Record<string, unknown>;
}

export type SchemaWithMetadata = Schema & {
  'x-annotations'?: Annotation[];
  'x-java-type'?: string;
};

function createAnnotationsProperty(schema: SchemaWithMetadata): PropertyAssignment | null {
  const annotations = schema['x-annotations'];

  if (!annotations || annotations.length === 0) {
    return null;
  }

  const literals = annotations.map(({ name, attributes }) =>
    ts.factory.createObjectLiteralExpression([
      ts.factory.createPropertyAssignment('jvmType', ts.factory.createStringLiteral(name)),
      ...(attributes && Object.keys(attributes).length > 0
        ? [ts.factory.createPropertyAssignment('attributes', createExpressionFromValue(attributes))]
        : []),
    ]),
  );

  return ts.factory.createPropertyAssignment('annotations', ts.factory.createArrayLiteralExpression(literals));
}

function createJvmTypeProperty(schema: SchemaWithMetadata): PropertyAssignment | null {
  const javaType = schema['x-java-type'];

  return javaType ? ts.factory.createPropertyAssignment('jvmType', ts.factory.createStringLiteral(javaType)) : null;
}

export function process(schema: Schema): ObjectLiteralExpression | null {
  const schemaWithMetadata = schema as SchemaWithMetadata;

  const properties = [createAnnotationsProperty(schemaWithMetadata), createJvmTypeProperty(schemaWithMetadata)].filter(
    Boolean,
  ) as PropertyAssignment[];

  return properties.length > 0 ? ts.factory.createObjectLiteralExpression(properties) : null;
}
