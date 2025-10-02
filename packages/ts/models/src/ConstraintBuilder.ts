import type { EmptyObject } from 'type-fest';
import {
  $assertSupportedModel,
  type Constraint,
  type ConstraintFn,
  type NonAttributedConstraint,
  $defaultValue,
  $name,
  type AnyObject,
  Model,
  type Value,
} from './Model.js';

/**
 * A builder class for declaring constraints.
 * @internal low-level API for internal use only.
 *
 * @typeParam V - The value type of the constraint.
 * @typeParam N - The name of the constraint.
 * @typeParam A - The attributes for the constraint function. It is an object with string
 * keys and arbitrary values. When the `value` attribute is the only one,
 * the value can be given directly as the first argument of the constraint
 * function; otherwise the attribute object should be provided.
 */
export class ConstraintBuilder<V = unknown, const N extends string = string, A extends AnyObject = EmptyObject> {
  private supportedModel: Model<V>;
  private [$name]: N | undefined;
  private readonly attributeDefaults: Required<A>;

  constructor() {
    this.supportedModel = Model as unknown as Model<V>;
    this.attributeDefaults = {} as unknown as Required<A>;
  }

  /**
   * Sets the model that the constraint is applicable to.
   *
   * @param supportedModel - The model that the constraint is applicable to.
   */
  model<const M extends Model<V>>(supportedModel: M): ConstraintBuilder<Value<M>, N, A> {
    this.supportedModel = supportedModel;
    return this as any;
  }

  /**
   * Sets the name of the constraint.
   *
   * @param name - the name of the constraint.
   * @returns The current builder instance updated with the new name.
   */
  name<const NN extends N>(name: NN): ConstraintBuilder<V, N & NN, A> {
    this[$name] = name;
    return this as any;
  }

  /**
   * Defines a new attribute for the constraint.
   *
   * @param name - the name of the attribute.
   * @param model - the model of the attribute value.
   * @returns The current builder instance updated with the new attribute.
   */
  attribute<AN extends string, AV>(
    name: AN,
    model: Model<AV>,
  ): ConstraintBuilder<
    V,
    N,
    (A extends EmptyObject ? AnyObject : A) & Readonly<undefined extends AV ? Partial<Record<AN, AV>> : Record<AN, AV>>
  > {
    (this.attributeDefaults as Record<string, unknown>)[name] = model[$defaultValue];
    return this as any;
  }

  /**
   * Builds the constraint declaration. On the typing level, it checks if all the
   * constraint parts are set correctly and raises an error if not.
   *
   * @returns The constraint declaration.
   */
  build(this: string extends N ? never : this): NonAttributedConstraint<V | undefined, N, A> {
    const name = this[$name];
    const { attributeDefaults, supportedModel } = this;

    function assertSupportedModel(model: Model<V>): void {
      if (!(model instanceof supportedModel)) {
        throw new Error(`The constraint "${name}" is not applicable to the model "${model[$name]}".`);
      }
    }

    let NonAttributedConstraint = ((valueOrAttributes?: unknown) => {
      const attributes: Required<A> = {
        ...attributeDefaults,
        ...(typeof valueOrAttributes === 'object' && valueOrAttributes !== null
          ? valueOrAttributes
          : { value: valueOrAttributes }),
      };

      return Object.defineProperties(Object.create(NonAttributedConstraint), {
        attributes: { value: attributes },
      }) as Constraint<V | undefined, N, A>;
    }) as unknown as ConstraintFn<V | undefined, A>;

    NonAttributedConstraint = Object.defineProperties(NonAttributedConstraint, {
      name: { value: name },
      [$assertSupportedModel]: { value: assertSupportedModel },
    }) as NonAttributedConstraint<V | undefined, N, A>;

    return NonAttributedConstraint as NonAttributedConstraint<V | undefined, N, A>;
  }
}
