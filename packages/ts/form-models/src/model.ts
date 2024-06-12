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

export const $key = Symbol();
export const $name = Symbol();
export const $owner = Symbol();
export const $meta = Symbol();
export const $optional = Symbol();
export const $defaultValue = Symbol();

export const $enum = Symbol();
export const $members = Symbol();
export const $itemModel = Symbol();

export interface Model<T = unknown> {
  readonly [$key]: keyof any;
  readonly [$name]: string;
  readonly [$owner]: Model | ModelOwner;
  readonly [$meta]: ModelMetadata;
  readonly [$optional]: boolean;
  readonly [$defaultValue]: T;
  [Symbol.hasInstance](o: unknown): o is this;
  toString(): string;
}

export type ExtendedModel<T = unknown, C extends object = EmptyRecord> = C &
  Model<T> &
  Readonly<{
    [K in keyof T]: ExtendedModel<T[K]>;
  }>;

export const Model = Object.create(null, {
  [$key]: {},
  [$name]: {
    value: 'Model',
  },
  [$owner]: {},
  [$meta]: {},
  [$optional]: {
    value: false,
  },
  [$defaultValue]: {
    value: undefined,
  },
  [Symbol.hasInstance]: {
    value(o: unknown) {
      return typeof o === 'object' && o != null && Object.prototype.isPrototypeOf.call(this, o);
    },
  },
  toString: {
    value(this: Model) {
      return `${String(this[$owner])} / ${String(this[$key])}${this[$optional] ? '?' : ''}: ${this[$name]}`;
    },
  },
});
