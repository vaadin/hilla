import type { EmptyObject } from 'type-fest';
import type { ObjectModel } from './core';
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
} from './model.js';

const { create, defineProperty, getPrototypeOf, getOwnPropertyDescriptor, fromEntries, entries } = Object;

export type ModelBuilderPropertyOptions = Readonly<{
  meta?: ModelMetadata;
}>;

const $model = Symbol();

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
   */
  selfRefKeys: string;
};

/**
 * A builder class for creating all basic models.
 *
 * @typeParam V - The final value type of the model.
 * @typeParam EX - The extra properties of the model.
 * @typeParam F - The flags for the model constructor that allow to determine
 * specific characteristics of the model.
 *
 * @param base - The base model to extend.
 * @param defaultValueProvider - The function that provides the default value
 * for the model.
 *
 * @internal
 */
export class CoreModelBuilder<
  V,
  EX extends AnyObject = EmptyObject,
  F extends Flags = { named: false; selfRefKeys: never },
> {
  protected readonly [$model]: Model<V, EX>;

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
   * Defines a new property on the model. The property serves the purposes of storing the extra data for specific types
   * of models.
   *
   * @remarks
   * The key of the property should be a symbol to avoid conflicts with properties defined via
   * {@link ObjectModelBuilder}.
   *
   * @param key - The key of the property.
   * @param value - The descriptor of the property.
   * @returns The current builder instance.
   */
  define<DK extends symbol, DV>(
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
   * @param defaultValueProvider - The function that provides the default value for the
   * model.
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
   * Builds the model.
   *
   * @returns The model.
   */
  build(this: F['named'] extends true ? this : never): Model<V, EX> {
    return this[$model];
  }
}

const propertyRegistry = new WeakMap<Model, Record<string, Model>>();

/**
 * A builder class for creating object models.
 *
 * @typeParam V - The final value type of the model.
 * @typeParam CV - The current value type of the model. It changes as the model
 * is being built and defines if the
 * {@link ObjectModelBuilder.build} method can be called.
 * @typeParam EX - The extra properties of the model.
 * @typeParam F - The flags for the model constructor that allow to determine
 * specific characteristics of the model.
 *
 * @internal
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
  declare ['define']: <DK extends symbol, DV>(
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
   * @param options - Additional options for the property.
   */
  property<PK extends string & keyof V, EXK extends AnyObject = EmptyObject>(
    key: PK,
    model: Model<V[PK], EXK> | ((model: Model<V, EX & Readonly<Record<PK, Model<V[PK], EXK>>>>) => Model<V[PK], EXK>),
    options?: ModelBuilderPropertyOptions,
  ): Extract<V[PK], V> extends never
    ? ObjectModelBuilder<V, CV & Readonly<Record<PK, V[PK]>>, EX & Readonly<Record<PK, Model<V[PK], EXK>>>, F>
    : ObjectModelBuilder<
        V,
        CV & Readonly<Record<PK, V[PK]>>,
        EX,
        { named: F['named']; selfRefKeys: F['selfRefKeys'] | PK }
      > {
    defineProperty(this[$model], key, {
      enumerable: true,
      get(this: Model<V, EX & Readonly<Record<PK, Model<V[PK], EXK>>>>) {
        if (!propertyRegistry.has(this)) {
          propertyRegistry.set(this, {});
        }

        const props = propertyRegistry.get(this)!;

        props[key] ??= new CoreModelBuilder<V[PK], EXK, { named: true; selfRefKeys: never }>(
          typeof model === 'function' ? model(this) : model,
        )
          .define($key, { value: key })
          .define($owner, { value: this })
          .define($meta, { value: options?.meta })
          .build();

        return props[key];
      },
    });

    return this as any;
  }

  /**
   * {@inheritDoc CoreModelBuilder.build}
   */
  declare build: (
    this: F['named'] extends true ? (CV extends V ? this : never) : never,
  ) => ObjectModel<V, EX, F['selfRefKeys']>;
}
