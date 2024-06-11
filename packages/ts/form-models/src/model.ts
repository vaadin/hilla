/* eslint-disable import/no-mutable-exports, @typescript-eslint/class-literal-property-style */
import type { Constructor } from 'type-fest';

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

/**
 * The model hierarchy root type
 */
export interface ModelOwner<T = unknown> {
  value: T;
}

export declare enum Enum {}

export type EmptyRecord = Record<never, never>;

export type ModelConstructor<T = unknown, C extends object = EmptyRecord> = C &
  Constructor<ExtendedModel<T>> &
  Readonly<{
    optional: boolean;
    defaultValue: T;
  }>;

export type ExtendedModel<T> = Model<T> &
  Readonly<{
    [K in keyof T]: ExtendedModel<T[K]>;
  }>;

export class Model<T = unknown> {
  static get optional(): boolean {
    return false;
  }

  static get defaultValue(): unknown {
    return undefined;
  }

  static getKey(model: Model): keyof any {
    return model.#key;
  }

  static getOwner(model: Model): Model | ModelOwner {
    return model.#owner;
  }

  static getMeta(model: Model): ModelMetadata | undefined {
    return model.#meta;
  }

  declare ['constructor']: typeof Model;

  readonly #key: keyof any;
  readonly #owner: Model | ModelOwner;
  readonly #meta?: ModelMetadata;

  constructor(key: keyof any, owner: Model | ModelOwner, meta?: ModelMetadata) {
    this.#key = key;
    this.#owner = owner;
    this.#meta = meta;
  }

  toString(): string {
    const { name, optional } = this.constructor;
    return `${String(this.#owner)} / ${String(this.#key)}${optional ? '?' : ''}: ${name}`;
  }
}
