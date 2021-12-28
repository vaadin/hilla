import {
  isNonComposedRegularSchema,
  NonComposedRegularSchema,
  Schema,
} from '@vaadin/generator-typescript-core/Schema.js';

export type AnnotatedSchema = NonComposedRegularSchema & Readonly<{ 'x-annotations': ReadonlyArray<string> }>;

export type ValidationConstrainedSchema = NonComposedRegularSchema &
  Readonly<{ 'x-validation-constraints': ReadonlyArray<Annotation> }>;

export function isAnnotatedSchema(schema: Schema): schema is AnnotatedSchema {
  return isNonComposedRegularSchema(schema) && 'x-annotations' in schema;
}

export function isValidationConstrainedSchema(schema: Schema): schema is ValidationConstrainedSchema {
  return isNonComposedRegularSchema(schema) && 'x-validation-constraints' in schema;
}

export type AnnotationPrimitiveAttribute = boolean | number | string;
export type AnnotationAttribute = AnnotationPrimitiveAttribute | AnnotationNamedAttributes;
export interface AnnotationNamedAttributes {
  readonly [name: string]: AnnotationPrimitiveAttribute;
}

export interface Annotation {
  simpleName: string;
  attributes?: AnnotationNamedAttributes;
}
