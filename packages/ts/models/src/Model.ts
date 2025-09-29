import type { EmptyObject } from 'type-fest';

export interface JvmTypeRef {
  jvmType: string;
  genericArguments?: JvmTypeRef[];
}

export type AnnotationValue = AnnotationValue[] | JvmTypeRef | boolean | number | string | undefined;

export interface Annotation {
  jvmType: string;
  attributes?: Record<string, AnnotationValue>;
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
export type Target<T = unknown> = Readonly<{
  model?: Model<T>;
  value: T;
}>;

export const nothing = Symbol('nothing');

const detachedTarget: Target = Object.create(
  {
    toString: () => ':detached:',
  },
  {
    model: { value: undefined },
    value: { value: nothing },
  },
);

export declare enum Enum {}

export type AnyObject = Readonly<Record<never, never>>; // {}

/* eslint-disable tsdoc/syntax */
/**
 * The symbol that represents the {@link Model[$key]} property.
 */
export const $key = Symbol('key');

/**
 * The symbol that represents the {@link Model[$name]} property.
 */
export const $name = Symbol('name');

/**
 * The symbol that represents the {@link Model[$owner]} property.
 */
export const $owner = Symbol('owner');

/**
 * The symbol that represents the {@link Model[$meta]} property.
 */
export const $meta = Symbol('meta');

/**
 * The symbol that represents the {@link Model[$optional]} property.
 */
export const $optional: unique symbol = Symbol('optional');

/**
 * The symbol that represents the {@link Model[$defaultValue]} property.
 */
export const $defaultValue = Symbol('defaultValue');

/**
 * The symbol that represents the {@link EnumModel[$enumerate]} property.
 */
export const $enum = Symbol('enumerate');

/**
 * The symbol that represents the {@link UnionModel[$members]} property.
 */
export const $members = Symbol('members');

/* eslint-enable tsdoc/syntax */

/**
 * Extracts the value type the model represents.
 */
export type Value<M extends Model> = M extends Model<infer T> ? T : never;

/**
 * Extracts the list of extra properties of the model.
 */
export type Extensions<M extends Model> = M extends Model<unknown, infer EX> ? EX : EmptyObject;

/**
 * Extracts the list of self-referencing properties of the model.
 */
export type References<M extends Model> = M extends Model<unknown, AnyObject, infer R> ? R : never;

/**
 * A model that represents a specific type of data.
 *
 * @typeParam V - The type of the data described by the model.
 * @typeParam EX - The extra properties of the model. It could be either a model
 * that represents a property of the object the current model describe, or a
 * model-specific metadata. It's recommended to use a symbol as a key for the
 * metadata property to avoid the potential naming conflicts with the described
 * object properties.
 * @typeParam R - The keys of the self-referencing properties of the model.
 *
 * @remarks
 * Since we know the full model definition only on this step, the `R` type
 * parameter is essential to describe a model with self-reference properties.
 */
export type Model<V = unknown, EX extends AnyObject = EmptyObject, R extends keyof any = never> = EX &
  Readonly<{
    [P in R]: Model<V | undefined, EX, R>;
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
    [$owner]: Model | Target;

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
    [Symbol.hasInstance](value: any): value is Model<V, EX, R>;
    toString(): string;
  }>;

/**
 * A function that provides a default value for a model.
 *
 * @typeParam V - The type of the data described by the model.
 * @typeParam EX - The extra properties of the model.
 * @typeParam R - The keys of the self-referencing properties of the model.
 */
export type DefaultValueProvider<V, EX extends AnyObject = EmptyObject, R extends keyof any = never> = (
  model: Model<V, EX, R>,
) => V;

export const Model: Model = Object.create(null, {
  [$key]: {
    value: 'model',
  },
  [$name]: {
    value: 'unknown',
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
      // eslint-disable-next-line @typescript-eslint/no-base-to-string
      return `[${String(this[$owner])} / ${String(this[$key])}${this[$optional] ? '?' : ''}] ${this[$name]}`;
    },
  },
});

export type ModelConverter<M extends Model, IM extends Model> = (model: IM) => M;
