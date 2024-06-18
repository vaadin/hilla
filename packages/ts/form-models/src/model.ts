/* eslint-disable import/no-mutable-exports, @typescript-eslint/class-literal-property-style */
import type { EmptyObject } from 'type-fest';

export interface JvmTypeRef {
  jvmType: string;
  genericArguments?: JvmTypeRef[];
}

export type AnnotationValue = AnnotationValue[] | JvmTypeRef | boolean | number | string | undefined;

export interface Annotation {
  jvmType: string;
  arguments: Record<string, AnnotationValue>;
}

/**
 * The metadata of a model.
 */
export interface ModelMetadata {
  jvmType?: string;
  annotations?: Annotation[];
}

export declare enum Enum {}

export type AnyObject = Readonly<Record<never, never>>;

export const $key = Symbol('key');
export const $name = Symbol('name');
export const $owner = Symbol('owner');
export const $meta = Symbol('meta');
export const $optional = Symbol('optional');
export const $defaultValue = Symbol('defaultValue');

export const $enum = Symbol('enum');
export const $members = Symbol('members');
export const $itemModel = Symbol('itemModel');

export type Value<M extends Model> = M extends Model<infer T> ? T : never;

/**
 * A model that represents a specific type of data.
 */
export type Model<T = unknown, C extends object = EmptyObject, R extends string = never> = C &
  Readonly<{
    [P in R]: Model<T, C, R>;
  }> &
  Readonly<{
    /**
     * The key of the model in the owner model.
     */
    [$key]?: keyof any;
    /**
     * The name of the model.
     */
    [$name]: string;
    /**
     * The owner model of the model. If there is no owner, the value is `undefined`.
     */
    [$owner]?: Model;
    /**
     * The metadata of the model.
     */
    [$meta]?: ModelMetadata;
    /**
     * Whether the model is optional. It describes if the data described by this model is nullable.
     */
    [$optional]: boolean;
    /**
     * The default value of the model.
     */
    [$defaultValue]: T;
    [Symbol.toStringTag]: string;
    [Symbol.hasInstance](value: any): value is Model<T, C>;
    toString(): string;
  }>;

export type DefaultValueProvider<T, C extends object> = (model: Model<T, C>) => T;

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
