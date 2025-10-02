/* eslint-disable @typescript-eslint/consistent-indexed-object-style */
import type { EmptyObject } from 'type-fest';
import {
  $defaultValue,
  $key,
  $meta,
  $name,
  $optional,
  $owner,
  type AnyObject,
  type DefaultValueProvider,
  type Model,
  type ModelConverter,
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
export class CoreModelBuilder<V, EX extends AnyObject = EmptyObject, F extends Flags = { named: false }> {
  protected readonly [$model]: Model<V, EX>;

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
  ): CoreModelBuilder<V, EX & { readonly [key in DK]: DV }, F> {
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
  name(name: string): CoreModelBuilder<V, EX, { named: true }> {
    return this.define($name, { value: name }) as any;
  }

  /**
   * Builds the model. On the typing level, it checks if all the model parts are
   * set correctly and raises an error if not.
   *
   * @returns The model.
   */
  build(this: F['named'] extends true ? this : never): Model<V, EX> {
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
  F extends Flags = { named: false },
> extends CoreModelBuilder<V, EX, F> {
  constructor(base: Model) {
    super(base, (m) => {
      const result: Record<string, unknown> = {};

      // eslint-disable-next-line no-restricted-syntax
      for (const key in m) {
        const keyModel = m[key as keyof Model<V, EX>] as Model;
        result[key] = keyModel[$optional] ? undefined : keyModel[$defaultValue];
        // defineProperty(result, key, {
        //   enumerable: true,
        //   value: keyModel[$optional] ? undefined : keyModel[$defaultValue],
        // });
      }

      return result as V;
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
  ): ObjectModelBuilder<NV & V, CV, EX, { named: true }> {
    return this.name(name) as any;
  }

  /**
   * {@inheritDoc CoreModelBuilder.define}
   */
  declare ['define']: <const DK extends symbol, DV>(
    key: DK,
    value: TypedPropertyDescriptor<DV>,
  ) => ObjectModelBuilder<V, CV, EX & { readonly [key in DK]: DV }, F>;

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
   */
  property<const PK extends string & keyof V, const M extends Model<V[PK]>>(
    key: PK,
    model: M,
  ): ObjectModelBuilder<
    V,
    EmptyObject extends CV
      ? { readonly [key in PK]: V[PK] }
      : {
          readonly [key in keyof CV | PK]: key extends PK ? V[PK] : key extends keyof CV ? CV[key] : never;
        },
    EmptyObject extends EX
      ? { readonly [key in PK]: M }
      : {
          readonly [key in keyof EX | PK]: key extends PK ? M : key extends keyof EX ? EX[key] : never;
        },
    F
  >;
  property<const PK extends string & keyof V, const M extends Model<V[PK]>>(
    key: PK,
    // eslint-disable-next-line @typescript-eslint/unified-signatures
    model: ModelConverter<M, Model<V, EX>>,
  ): ObjectModelBuilder<
    V,
    EmptyObject extends CV
      ? { readonly [key in PK]: V[PK] }
      : {
          readonly [key in keyof CV | PK]: key extends PK ? V[PK] : key extends keyof CV ? CV[key] : never;
        },
    EmptyObject extends EX
      ? { readonly [key in PK]: M }
      : {
          readonly [key in keyof EX | PK]: key extends PK ? M : key extends keyof EX ? EX[key] : never;
        },
    F
  >;
  property<PK extends string & keyof V, M extends Model<V[PK]>>(
    key: PK,
    model: M | ((model: Model<V, EX>) => M),
  ): unknown {
    defineProperty(this[$model], key, {
      enumerable: true,
      get(this: Model<V, EX & { readonly [key in PK]: M }>) {
        if (!propertyRegistry.has(this)) {
          propertyRegistry.set(this, {});
        }

        const props = propertyRegistry.get(this)!;

        props[key] ??= new CoreModelBuilder<V[PK], EmptyObject, { named: true }>(
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
  declare build: (this: F['named'] extends true ? (CV extends V ? this : never) : never) => ObjectModel<V, EX>;
}
