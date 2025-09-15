import type { EmptyObject } from 'type-fest';
import {
  $defaultValue,
  $key,
  $meta,
  $name,
  $owner,
  type AnyObject,
  type DefaultValueProvider,
  type Model,
  type ModelMetadata,
} from './Model.js';
import type { ObjectModel } from './models.js';

const { create, defineProperty } = Object;

const $model = Symbol('model');

/**
 * The flags for the model constructor that allow to determine specific characteristics of the model.
 */
export type Flags = {
  /**
   * Defines if the model is named.
   */
  named: boolean;

  /**
   * The keys of the self-referencing properties.
   *
   * @remarks
   * The problem of self-reference models is that they cannot have intermediate
   * type because during the property definition, the model itself is in the
   * middle of construction. That's why we define the specific type-only flag
   * that allows us to know which model properties are self-referenced. We can
   * safely set it in the end of building using this flag.
   */
  selfRefKeys: keyof any;
};

/**
 * A builder class for creating all basic models.
 * @internal low-level API for internal use only.
 *
 * @typeParam V - The final value type of the model.
 * @typeParam EX - The extra properties of the model.
 * @typeParam F - The flags for the model constructor that allow to determine
 * specific characteristics of the model.
 */
export class CoreModelBuilder<
  V,
  EX extends AnyObject = EmptyObject,
  F extends Flags = { named: false; selfRefKeys: never },
> {
  protected readonly [$model]: Model<V, EX, F['selfRefKeys']>;

  /**
   * @param base - The base model to extend.
   * @param defaultValueProvider - The function that provides the default value
   * for the model.
   */
  constructor(base: Model, defaultValueProvider?: (model: Model<V, EX>) => V) {
    this[$model] = create(base);

    if (defaultValueProvider) {
      this.defaultValueProvider(defaultValueProvider);
    }
  }

  /**
   * Appends metadata to the model.
   *
   * @param value - The metadata to append.
   * @returns The current builder instance.
   */
  meta(value: ModelMetadata): this {
    this.define($meta, { value });
    return this;
  }

  /**
   * Defines a new property on the model. The property serves the purposes of
   * storing the extra data for specific types of models.
   *
   * @remarks
   * The key of the property should be a symbol to avoid conflicts with
   * properties defined via
   * {@link ObjectModelBuilder}.
   *
   * @param key - The key of the property.
   * @param value - The descriptor of the property.
   * @returns The current builder instance.
   */
  define<const DK extends symbol, const DV>(
    key: DK,
    value: TypedPropertyDescriptor<DV>,
  ): CoreModelBuilder<V, EX & Readonly<Record<DK, DV>>, F> {
    defineProperty(this[$model], key, value);
    return this as any;
  }

  /**
   * Sets the default value provider for the model. This is an alternative way
   * to provide the default value for the model if for some reason using the
   * constructor parameter is undesired.
   *
   * @param defaultValueProvider - The function that provides the default value
   * for the model.
   * @returns The current builder instance.
   */
  defaultValueProvider(defaultValueProvider: DefaultValueProvider<V, EX>): this {
    this.define($defaultValue, {
      get(this: Model<V, EX>) {
        return defaultValueProvider(this);
      },
    });
    return this;
  }

  /**
   * Sets the name of the model. The name is used for debugging purposes and is
   * displayed in the string representation. Setting the name is required;
   * otherwise, the {@link CoreModelBuilder.build} method won't be available.
   *
   * @param name - The name of the model.
   * @returns The current builder instance.
   */
  name(name: string): CoreModelBuilder<V, EX, { named: true; selfRefKeys: F['selfRefKeys'] }> {
    return this.define($name, { value: name }) as any;
  }

  /**
   * Builds the model. On the typing level, it checks if all the model parts are
   * set correctly and raises an error if not.
   *
   * @returns The model.
   */
  build(this: F['named'] extends true ? this : never): Model<V, EX, F['selfRefKeys']> {
    return this[$model];
  }
}

/**
 * A registry for the property models of the object model. Since the property
 * registration is lazy, we cannot store the property models directly on the
 * object model, so the registry plays a role of a private storage for them.
 *
 * @internal
 */
const propertyRegistry = new WeakMap<Model, Record<string, Model>>();

/**
 * A builder class for creating object models.
 * @internal low-level API for internal use only.
 *
 * @typeParam V - The final value type of the model.
 * @typeParam CV - The current value type of the model. It changes as the model
 * is being built and defines if the
 * {@link ObjectModelBuilder.build} method can be called.
 * @typeParam EX - The extra properties of the model.
 * @typeParam F - The flags for the model constructor that allow to determine
 * specific characteristics of the model.
 */
export class ObjectModelBuilder<
  V extends AnyObject,
  CV extends AnyObject = EmptyObject,
  EX extends AnyObject = EmptyObject,
  F extends Flags = { named: false; selfRefKeys: never },
> extends CoreModelBuilder<V, EX, F> {
  constructor(base: Model) {
    super(base, (m) => {
      const result = create(null);

      // eslint-disable-next-line no-restricted-syntax
      for (const key in m) {
        defineProperty(result, key, {
          enumerable: true,
          get: () => (m[key as keyof Model<V, EX>] as Model)[$defaultValue],
        });
      }

      return result;
    });
  }

  /**
   * The method that should follow the {@link m.extend} method. It allows to
   * declare the extension for the model and properly name it.
   *
   * @param name - The name of the model.
   */
  object<NV extends AnyObject>(
    this: F['named'] extends false ? this : never,
    name: string,
  ): ObjectModelBuilder<NV & V, CV, EX, { named: true; selfRefKeys: F['selfRefKeys'] }> {
    return this.name(name) as any;
  }

  /**
   * {@inheritDoc CoreModelBuilder.define}
   */
  declare ['define']: <const DK extends symbol, DV>(
    key: DK,
    value: TypedPropertyDescriptor<DV>,
  ) => ObjectModelBuilder<V, CV, EX & Readonly<Record<DK, DV>>, F>;

  /**
   * {@inheritDoc CoreModelBuilder.meta}
   */
  declare ['meta']: (value: ModelMetadata) => this;

  /**
   * Defines a new model property on the model. Unlike the
   * {@link ObjectModelBuilder.define}, this property is public and allows the
   * user to interact with the model data structure. It also updates the current
   * value type of the model to make it closer to the final value type.
   *
   * @param key - The key of the property.
   * @param model - The model of the property value. You can also provide a
   * function that produces the model based on the current model.
   *
   * @returns The current builder instance updated with the new property type.
   * In case there is a self-referencing property, the {@link Flags.selfRefKeys}
   * flag for the specific property is set.
   */
  property<const PK extends string & keyof V, const M extends Model<V[PK]>>(
    key: PK,
    model: M,
  ): ObjectModelBuilder<V, CV & Readonly<Record<PK, V[PK]>>, EX & Readonly<Record<PK, M>>, F>;
  property<const PK extends string & keyof V, const M extends Model<V[PK]>>(
    key: PK,
    model: (model: Model<V, EX>) => M,
  ): // Workaround for the self-referencing models.
  // If the type of the model property is the model itself,
  V extends V[PK]
    ? // Then we set a flag of the model that it contains a self-reference
      // property.
      ObjectModelBuilder<
        V,
        CV & Readonly<Record<PK, V[PK]>>,
        EX,
        {
          // Just inheriting the current flag.
          named: F['named'];
          // Adding the property name to all existing self-referencing
          // properties.
          selfRefKeys: F['selfRefKeys'] | PK;
        }
      >
    : // Otherwise we simply extend the model with the property, and update the
      // current value type of the model.
      ObjectModelBuilder<V, CV & Readonly<Record<PK, V[PK]>>, EX & Readonly<Record<PK, M>>, F>;
  property<const PK extends string & keyof V, const M extends Model<V[PK]>>(
    key: PK,
    model: M | ((model: Model<V, EX>) => M),
  ): unknown {
    defineProperty(this[$model], key, {
      enumerable: true,
      get(this: Model<V, EX & Readonly<Record<PK, M>>>) {
        if (!propertyRegistry.has(this)) {
          propertyRegistry.set(this, {});
        }

        const props = propertyRegistry.get(this)!;

        props[key] ??= new CoreModelBuilder<V[PK], EmptyObject, { named: true; selfRefKeys: never }>(
          typeof model === 'function' ? model(this) : model,
        )
          .define($key, { value: key })
          .define($owner, { value: this })
          .build();

        return props[key];
      },
    });

    return this;
  }

  /**
   * {@inheritDoc CoreModelBuilder.build}
   */
  declare build: (
    this: F['named'] extends true ? (CV extends V ? this : never) : never,
  ) => ObjectModel<V, EX, F['selfRefKeys']>;
}
