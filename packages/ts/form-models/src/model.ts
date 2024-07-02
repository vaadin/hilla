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

/**
 * The target to which a model is attached. It could be a Binder instance, a
 * Signal or another object. However, it could never be another model.
 */
export type AttachTarget<T = unknown> = Readonly<{
  model?: Model<T>;
  value: T;
}>;

export const nothing = Symbol();

const detachedTarget: AttachTarget = Object.create(
  {
    toString: () => ':detached:',
  },
  {
    model: { value: undefined },
    value: { value: nothing },
  },
);

export declare enum Enum {}

export type AnyObject = Readonly<Record<never, never>>;

export const $key = Symbol('key');
export const $name = Symbol('name');
export const $owner = Symbol('owner');
export const $meta = Symbol('meta');
export const $optional = Symbol('optional');
export const $defaultValue = Symbol('value');

export const $enum = Symbol('enum');
export const $members = Symbol('members');
export const $itemModel = Symbol('itemModel');

export type Value<M extends Model> = M extends Model<infer T> ? T : never;
export type Extensions<M extends Model> = M extends Model<unknown, infer EX> ? EX : EmptyObject;
export type References<M extends Model> = M extends Model<unknown, AnyObject, infer R> ? R : never;

/**
 * A model that represents a specific type of data.
 */
export type Model<V = unknown, EX extends AnyObject = EmptyObject, R extends string = never> = EX &
  Readonly<{
    [P in R]: Model<V, EX, R>;
  }> &
  Readonly<{
    /**
     * The key of the model in the owner model.
     */
    [$key]: keyof any;

    /**
     * The name of the model. For attached models, the name will be prefixed
     * with the `@` symbol.
     */
    [$name]: string;

    /**
     * The owner model of the model. For detached models, the owner will always
     * be a specific global object `detachedTarget`.
     */
    [$owner]: AttachTarget | Model;

    /**
     * The metadata of the model.
     */
    [$meta]?: ModelMetadata;

    /**
     * Whether the model is optional. It describes if the data described by
     * this model is nullable.
     */
    [$optional]: boolean;

    /**
     * The default value of the model.
     */
    [$defaultValue]: V;
    [Symbol.toStringTag]: string;
    [Symbol.hasInstance](value: any): value is Model<V, EX>;
    toString(): string;
  }>;

/**
 * A function that provides a default value for a model.
 */
export type DefaultValueProvider<T, C extends object> = (model: Model<T, C>) => T;

export const Model: Model = Object.create(null, {
  [$key]: {
    value: 'model',
  },
  [$name]: {
    value: 'Model',
  },
  [$owner]: {
    value: detachedTarget,
  },
  [$meta]: {},
  [$optional]: {
    value: false,
  },
  [$defaultValue]: {},
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
      return `[${String(this[$owner])} / ${String(this[$key])}${this[$optional] ? '?' : ''}] ${this[$name]}`;
    },
  },
});
