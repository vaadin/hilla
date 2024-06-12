import {
  $defaultValue,
  $key,
  $meta,
  $name,
  $owner,
  type EmptyRecord,
  type ExtendedModel,
  type Model,
  type ModelMetadata,
} from './model.js';

export type ModelBuilderPropertyOptions = Readonly<{
  meta?: ModelMetadata;
}>;

const $base = Symbol();
const $properties = Symbol();

export class CoreModelBuilder<T, C extends object = EmptyRecord> {
  static from<T>(base: ExtendedModel, defaultValueProvider?: () => T): CoreModelBuilder<T> {
    return new CoreModelBuilder(base, defaultValueProvider);
  }

  protected readonly [$base]: ExtendedModel;
  protected readonly [$properties]: Record<keyof any, PropertyDescriptor> = {};

  protected constructor(base: ExtendedModel, defaultValueProvider?: () => T) {
    this[$base] = base;

    if (defaultValueProvider) {
      this[$properties].defaultValue = {
        get: defaultValueProvider,
      };
    }
  }

  meta(value: ModelMetadata): this {
    this.define($meta, value);
    return this;
  }

  define<K extends symbol, V>(key: K, value: V): CoreModelBuilder<T, C & Record<K, V>> {
    this[$properties][key] = { value };
    return this as CoreModelBuilder<T, C & Record<K, V>>;
  }

  name(name: string): this {
    this.define($name, name);
    return this;
  }

  build(): ExtendedModel<T, C> {
    return Object.create(this[$base], this[$properties]);
  }
}

export class ObjectModelBuilder<
  T extends object,
  U extends object = object,
  C extends object = EmptyRecord,
> extends CoreModelBuilder<T, C> {
  static extend<T extends object, U extends object = object>(base: ExtendedModel): ObjectModelBuilder<T, U> {
    return new ObjectModelBuilder<T, U>(base);
  }

  protected constructor(base: ExtendedModel) {
    super(
      base,
      () =>
        Object.fromEntries(
          Object.entries(this[$properties]).map(
            ([key, descriptor]) => [key, (descriptor.value as Model)[$defaultValue]] as const,
          ),
        ) as T,
    );
  }

  declare ['build']: () => U extends T ? ExtendedModel<T, C> : never;
  declare ['define']: <K extends symbol, V>(key: K, value: V) => ObjectModelBuilder<T, U, C & Readonly<Record<K, V>>>;
  declare ['name']: (name: string) => this;
  declare ['meta']: (value: ModelMetadata) => this;

  property<K extends keyof T>(
    key: K,
    model: ExtendedModel<T[K]>,
    options?: ModelBuilderPropertyOptions,
  ): ObjectModelBuilder<T, Readonly<Record<K, T[K]>> & U, C> {
    this[$properties][key] = {
      enumerable: true,
      value: ObjectModelBuilder.extend(model)
        .define($key, key)
        .define($owner, this)
        .define($meta, options?.meta)
        .build(),
    };

    return this as ObjectModelBuilder<T, U, C & Readonly<Record<K, T[K]>>>;
  }
}
