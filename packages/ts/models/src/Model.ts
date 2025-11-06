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
export type Target<T = unknown> = {
  readonly model?: Model<T>;
  readonly value: T;
};

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
 * The symbol that represents the constraints property of the {@link Model} type.
 */
export const $constraints = Symbol('constraints');

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
export type Extensions<M extends Model> = M extends Model<unknown, infer EX> ? EX : AnyObject;

/**
 * The symbol that represents the {@link Constraint} method asserting supported model.
 */
export const $assertSupportedModel = Symbol('assertSupportedModel');

/**
 * The constraint function type.
 */
export type ConstraintFn<V = unknown, A extends AnyObject = AnyObject> = EmptyObject extends A
  ? (attributes?: A) => Constraint<V>
  : { readonly value: never } extends A
    ? (valueOrAttributes: (A & { readonly value: unknown })['value'] | A) => Constraint<V>
    : (attributes: A) => Constraint<V>;

/**
 * The constraint type that doesn't necessarily have attributes specified.
 *
 * @typeParam V - The type of the data described by the model.
 * @typeParam N - The name of the constraint.
 * @typeParam A - The attributes of the constraint.
 */
export type NonAttributedConstraint<
  V = unknown,
  N extends string = string,
  A extends AnyObject = AnyObject,
> = ConstraintFn<V, A> &
  Readonly<{
    attributes: A;
    name: N;
    [$assertSupportedModel](model: Model<V>): void;
  }>;

/**
 * The constraint type with specified attributes.
 *
 * @typeParam V - The type of the data described by the model.
 * @typeParam N - The name of the constraint.
 * @typeParam A - The attributes of the constraint.
 */
export type Constraint<V = unknown, N extends string = string, A extends AnyObject = AnyObject> = Readonly<{
  attributes: EmptyObject extends A ? A : Required<A>;
  name: N;
  [$assertSupportedModel](model: Model<V>): void;
}>;

/**
 * A model that represents a specific type of data.
 *
 * @typeParam V - The type of the data described by the model.
 * @typeParam EX - The extra properties of the model. It could be either a model
 * that represents a property of the object the current model describe, or a
 * model-specific metadata. It's recommended to use a symbol as a key for the
 * metadata property to avoid the potential naming conflicts with the described
 * object properties.
 *
 * @remarks
 * Since we know the full model definition only on this step, the `R` type
 * parameter is essential to describe a model with self-reference properties.
 */
export type Model<V = unknown, EX extends AnyObject = AnyObject> = EX & {
  /**
   * The key of the model in the owner model.
   */
  readonly [$key]: keyof any;

  /**
   * The name of the model. For attached models, the name will be prefixed
   * with the `@` symbol.
   */
  readonly [$name]: string;

  /**
   * The owner model of the model. For detached models, the owner will always
   * be a specific global object `detachedTarget`.
   */
  readonly [$owner]: Model | Target;

  /**
   * The metadata of the model.
   */
  readonly [$meta]?: ModelMetadata;

  /**
   * Whether the model is optional. It describes if the data described by
   * this model is nullable.
   */
  readonly [$optional]: boolean;

  /**
   * The list of validation constraints for the model.
   */
  readonly [$constraints]: readonly Constraint[];

  /**
   * The default value of the model.
   */
  readonly [$defaultValue]: V;
  readonly [Symbol.toStringTag]: string;
  [Symbol.hasInstance](value: any): value is Model<V, EX>;
  toString(): string;
};

/**
 * A function that provides a default value for a model.
 *
 * @typeParam V - The type of the data described by the model.
 * @typeParam EX - The extra properties of the model.
 * @typeParam R - The keys of the self-referencing properties of the model.
 */
export type DefaultValueProvider<V, EX extends AnyObject = AnyObject> = (model: Model<V, EX>) => V;

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
  [$constraints]: {
    value: [],
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
      // eslint-disable-next-line @typescript-eslint/no-base-to-string
      return `[${String(this[$owner])} / ${String(this[$key])}${this[$optional] ? '?' : ''}] ${this[$name]}`;
    },
  },
});

/**
 * The symbol marking source (parameter) type for model converter HKT
 * signatures.
 */
export declare const $sourceModel: unique symbol;

/**
 * The symbol marking target (return) type for model converter HKT
 * signatures.
 */
export declare const $targetModel: unique symbol;

/**
 * Base HKT signature for model converter functions. Corresponds to a function
 * such as.
 */
export type ModelConverter<SM extends Model = Model, TM extends Model = Model> = {
  readonly [$sourceModel]: SM;
  readonly [$targetModel]: TM;
};

/**
 * Gets the source type of the given model converter HKT signature.
 */
export type SourceModel<MC extends ModelConverter> = MC[typeof $sourceModel];

/**
 * Gets the target type of the given model converter HKT signature for the given
 * source type.
 */
export type TargetModel<MC extends ModelConverter, SM extends SourceModel<MC>> = (MC & {
  readonly [$sourceModel]: SM;
})[typeof $targetModel];

/**
 * Makes functional composition of two given model converter HKT signatures,
 * given A(x) and B(x) returns A(B(x)) HKT signature.
 */
export interface CompositeOf<A extends ModelConverter, B extends ModelConverter<Model, SourceModel<A>>>
  extends ModelConverter<SourceModel<B>> {
  readonly [$targetModel]: TargetModel<A, TargetModel<B, SourceModel<this>>>;
}
