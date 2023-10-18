export interface JvmTypeRef {
  jvmType: string;
  genericArguments?: JvmTypeRef[];
}

export type AnnotationPrimitiveValue = boolean | number | string | undefined;
export type AnnotationValue = AnnotationPrimitiveValue | AnnotationValue[] | JvmTypeRef;

export interface Annotation {
  jvmType: string;
  arguments: Record<string, AnnotationValue>;
}

export interface ModelMetadata {
  jvmType?: string;
  annotations?: Annotation[];
}
