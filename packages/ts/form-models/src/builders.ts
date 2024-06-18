import type { EmptyObject } from 'type-fest';
import type { ObjectModel } from './core';
import {
  $defaultValue,
  $key,
  $meta,
  $name,
  $owner,
  type DefaultValueProvider,
  type AnyObject,
  type Model,
  type ModelMetadata,
} from './model.js';

const { create, defineProperty, fromEntries, entries } = Object;

export type ModelBuilderPropertyOptions = Readonly<{
  meta?: ModelMetadata;
}>;

const $model = Symbol();

export type Flags = {
  named: boolean;
  selfRefKeys: string;
};

/**
 * A builder class for creating all basic models.
 *
 * @typeParam V - The final value type of the model.
 * @typeParam EX - The extra properties of the model.
 * @typeParam F - The flags for the model constructor that allow to determine specific characteristics of the model.
 *
 * @param base - The base model to extend.
 * @param defaultValueProvider - The function that provides the default value for the model.
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
   * Sets the default value provider for the model. This is an alternative way to provide the default value for the
   * model if for some reason using the constructor parameter is undesired.
   *
   * @param defaultValueProvider - The function that provides the default value for the model.
   * @returns The current builder instance.
   */
  defaultValueProvider(defaultValueProvider: DefaultValueProvider<V, EX>): this {
    defineProperty(this[$model], $defaultValue, {
      get(this: Model<V, EX>) {
        return defaultValueProvider(this);
      },
    });
    return this;
  }

  /**
   * Sets the name of the model. The name is used for debugging purposes and is displayed in the string representation.
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
  build(): F['named'] extends true ? Model<V, EX, F['selfRefKeys']> : never {
    return this[$model] as any;
  }
}

const propertyRegistry = new WeakMap<Model, Record<string, Model>>();

/**
 * A builder class for creating object models.
 *
 * @typeParam V - The final value type of the model.
 * @typeParam CV - The current value type of the model. It changes as the model is being built and defines if the
 * {@link ObjectModelBuilder.build} method can be called.
 * @typeParam EX - The extra properties of the model.
 * @typeParam F - The flags for the model constructor that allow to determine specific characteristics of the model.
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
    super(
      base,
      (m) =>
        fromEntries(entries<Model>(m).map(([key, child]) => [key, child[$defaultValue]])) as ReturnType<
          DefaultValueProvider<V, EX>
        >,
    );
  }

  /**
   * {@inheritDoc CoreModelBuilder.define}
   */
  declare ['define']: <DK extends symbol, DV>(
    key: DK,
    value: TypedPropertyDescriptor<DV>,
  ) => ObjectModelBuilder<V, CV, EX & Readonly<Record<DK, DV>>, F>;

  /**
   * {@inheritDoc CoreModelBuilder.name}
   */
  declare ['name']: <NV extends AnyObject>(
    name: string,
  ) => ObjectModelBuilder<NV, CV, EX, { named: true; selfRefKeys: F['selfRefKeys'] }>;

  /**
   * {@inheritDoc CoreModelBuilder.meta}
   */
  declare ['meta']: (value: ModelMetadata) => this;

  /**
   * Defines a new model property on the model. Unlike the {@link ObjectModelBuilder.define}, this property is public
   * and allows the user to interact with the model data structure. It also updates the current value type of the model
   * to make it closer to the final value type.
   *
   * @param key - The key of the property.
   * @param model - The model of the property value. You can also provide a function that produces the model based on
   * the current model.
   * @param options - Additional options for the property.
   */
  property<PK extends string & keyof V, CK extends AnyObject = EmptyObject>(
    key: PK,
    model: Model<V[PK], CK> | ((model: Model<V, EX & Readonly<Record<PK, Model<V[PK], CK>>>>) => Model<V[PK], CK>),
    options?: ModelBuilderPropertyOptions,
  ): Extract<V[PK], V> extends never
    ? ObjectModelBuilder<V, CV & Readonly<Record<PK, V[PK]>>, EX & Readonly<Record<PK, Model<V[PK], CK>>>, F>
    : ObjectModelBuilder<
        V,
        CV & Readonly<Record<PK, V[PK]>>,
        EX,
        { named: F['named']; selfRefKeys: F['selfRefKeys'] | PK }
      > {
    defineProperty(this[$model], key, {
      enumerable: true,
      get(this: Model<V, EX & Readonly<Record<PK, Model<V[PK], CK>>>>) {
        if (!propertyRegistry.has(this)) {
          propertyRegistry.set(this, {});
        }

        const props = propertyRegistry.get(this)!;

        props[key] ??= new CoreModelBuilder(typeof model === 'function' ? model(this) : model)
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
  declare ['build']: () => F['named'] extends true
    ? CV extends V
      ? ObjectModel<V, EX, F['selfRefKeys']>
      : never
    : never;
}
