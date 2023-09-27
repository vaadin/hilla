import type { Schema } from '@hilla/generator-typescript-core/Schema.js';
import ts, { type ObjectLiteralExpression, type PropertyAssignment } from 'typescript';

interface Annotation {
  name: string;
  attributes?: Record<string, unknown>;
}

export type SchemaWithMetadata = Schema & {
  'x-annotations'?: Annotation[];
  'x-java-type'?: string;
};

export class MetadataProcessor {
  process(schema: Schema): ObjectLiteralExpression | null {
    const schemaWithMetadata = schema as SchemaWithMetadata;

    const properties = [
      this.#createAnnotationsProperty(schemaWithMetadata),
      this.#createJavaTypeProperty(schemaWithMetadata),
    ].filter(Boolean) as PropertyAssignment[];

    if (properties.length === 0) {
      return null;
    }

    return ts.factory.createObjectLiteralExpression(properties);
  }

  #createAnnotationsProperty(schema: SchemaWithMetadata): PropertyAssignment | null {
    const annotations = schema['x-annotations'];
    const hasAnnotations = annotations && annotations.length > 0;
    if (!hasAnnotations) {
      return null;
    }

    const annotationLiterals = annotations.map((annotation) =>
      ts.factory.createObjectLiteralExpression([
        ts.factory.createPropertyAssignment('name', ts.factory.createStringLiteral(annotation.name)),
      ]),
    );

    return ts.factory.createPropertyAssignment(
      'annotations',
      ts.factory.createArrayLiteralExpression(annotationLiterals),
    );
  }

  #createJavaTypeProperty(schema: SchemaWithMetadata): PropertyAssignment | null {
    const javaType = schema['x-java-type'];
    if (!javaType) {
      return null;
    }

    return ts.factory.createPropertyAssignment('javaType', ts.factory.createStringLiteral(javaType));
  }
}
