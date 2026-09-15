import ts, { type Expression, type ObjectLiteralExpression, type PropertyAssignment } from '@typescript/typescript6';
import type { Schema } from '@vaadin/hilla-generator-core/Schema.js';

type AnnotationValue =
  | AnnotationValue[]
  | boolean
  | number
  | string
  | Readonly<Record<string, unknown>>
  | null
  | undefined;

interface Annotation {
  name: string;
  attributes?: Record<string, AnnotationValue>;
}

export type SchemaWithMetadata = Schema & {
  'x-annotations'?: Annotation[];
  'x-java-type'?: string;
};

function createObject(value: Readonly<Record<string, unknown>>): ObjectLiteralExpression {
  return ts.factory.createObjectLiteralExpression(
    Object.entries(value).map(([name, nested]) =>
      // eslint-disable-next-line @typescript-eslint/no-use-before-define
      ts.factory.createPropertyAssignment(name, createValue(nested as AnnotationValue)),
    ),
  );
}

function createValue(value: AnnotationValue): Expression {
  if (Array.isArray(value)) {
    return ts.factory.createArrayLiteralExpression(value.map(createValue));
  }

  switch (typeof value) {
    case 'string':
      return ts.factory.createStringLiteral(value);
    case 'number':
      return ts.factory.createNumericLiteral(value);
    case 'boolean':
      return value ? ts.factory.createTrue() : ts.factory.createFalse();
    case 'object':
      return value === null ? ts.factory.createNull() : createObject(value);
    default:
      return ts.factory.createIdentifier('undefined');
  }
}

function createAnnotationsProperty(schema: SchemaWithMetadata): PropertyAssignment | null {
  const annotations = schema['x-annotations'];

  if (!annotations || annotations.length === 0) {
    return null;
  }

  const literals = annotations.map(({ name, attributes }) =>
    ts.factory.createObjectLiteralExpression([
      ts.factory.createPropertyAssignment('jvmType', ts.factory.createStringLiteral(name)),
      ...(attributes ? [ts.factory.createPropertyAssignment('attributes', createObject(attributes))] : []),
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
