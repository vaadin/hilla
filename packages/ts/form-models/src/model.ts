/* eslint-disable import/no-mutable-exports, @typescript-eslint/class-literal-property-style */
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

export declare enum Enum {}

export type EmptyRecord = Record<keyof any, never>;

export const $key = Symbol('key');
export const $name = Symbol('name');
export const $owner = Symbol('owner');
export const $meta = Symbol('meta');
export const $optional = Symbol('optional');
export const $defaultValue = Symbol('defaultValue');

export const $enum = Symbol('enum');
export const $members = Symbol('members');
export const $itemModel = Symbol('itemModel');

export type ModelValue<T> = T extends boolean | number | string
  ? T
  : T extends typeof Enum
    ? T[keyof T]
    : T extends unknown[]
      ? T
      : T extends object
        ? T
        : undefined extends T
          ? Exclude<T, undefined>
          : unknown;

export interface Model<T = unknown> {
  readonly [$key]?: keyof any;
  readonly [$name]: string;
  readonly [$owner]?: Model;
  readonly [$meta]?: ModelMetadata;
  readonly [$optional]: boolean;
  readonly [$defaultValue]: T;
  readonly [Symbol.toStringTag]: string;
  [Symbol.hasInstance](value: any): boolean;
  toString(): string;
  [key: string]: unknown;
}

export type DefaultValueProvider<T, C extends object> = (model: C & Model<T>) => T;

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
  [Symbol.toStringTag]: {
    get(this: Model) {
      return this[$name];
    },
  },
  [Symbol.hasInstance]: {
    value(this: Model, o: unknown) {
      return typeof o === 'object' && o != null && (this === o || Object.prototype.isPrototypeOf.call(this, o));
    },
  },
  toString: {
    value(this: Model) {
      return `${String(this[$owner])} / ${String(this[$key])}${this[$optional] ? '?' : ''}: ${this[$name]}`;
    },
  },
});
