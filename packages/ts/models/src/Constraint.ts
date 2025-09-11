import type { EmptyObject } from 'type-fest';
import type { AnyObject, Model } from './Model.js';

/**
 * The symbol that represents the constraints property of the {@link Model} type.
 */
export const $constraints = Symbol('constraints');

/**
 * The model that has been constrained by a list of validation constraints.
 */
export type ConstrainedModel<M extends Model> = M & {
  /**
   * The list of validation constraints for the model.
   */
  readonly [$constraints]: readonly Constraint[];
};

export const $assertSupportedModel = Symbol('assertSupportedModel');

export type ConstraintFn<V = unknown, A extends AnyObject = EmptyObject> = EmptyObject extends A
  ? (attributes?: A) => Constraint<V>
  : { readonly value: never } extends A
    ? (valueOrAttributes: (A & { readonly value: unknown })['value'] | A) => Constraint<V>
    : (attributes: A) => Constraint<V>;

export type NonAttributedConstraint<
  V = unknown,
  N extends string = string,
  A extends AnyObject = EmptyObject,
> = ConstraintFn<V, A> &
  Readonly<{
    attributes: A;
    name: N;
    [$assertSupportedModel](model: Model<V>): void;
  }>;

export type Constraint<
  V = unknown,
  N extends string = string,
  A extends AnyObject = EmptyObject,
> = EmptyObject extends A
  ? NonAttributedConstraint<V, N, A>
  : NonAttributedConstraint<V, N, A> & {
      attributes: Required<A>;
    };
