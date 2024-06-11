import type { ExtendedModel, Model, ModelMetadata, ModelOwner, EmptyRecord, ModelConstructor } from './model.js';

export type ModelBuilderPropertyOptions = Readonly<{
  meta?: ModelMetadata;
}>;

export interface ModelBuilder<T, C extends object = EmptyRecord> {
  build(): ModelConstructor<T, C>;
  define<K extends keyof any, V>(key: K, value: V): ModelBuilder<T, C & Record<K, V>>;
  name(name: string): this;
}

export class CoreModelBuilder<T, C extends object = EmptyRecord> implements ModelBuilder<T, C> {
  static from<T>(base: ModelConstructor, defaultValueProvider?: () => T): CoreModelBuilder<T> {
    return new CoreModelBuilder(base, defaultValueProvider);
  }

  readonly #base: ModelConstructor;
  readonly #statics: Record<keyof any, PropertyDescriptor> = {};

  private constructor(base: ModelConstructor, defaultValueProvider?: () => T) {
    this.#base = base;

    if (defaultValueProvider) {
      this.#statics.defaultValue = {
        enumerable: true,
        get() {
          return defaultValueProvider();
        },
      };
    }
  }

  define<K extends keyof any, V>(key: K, value: V): ModelBuilder<T, C & Record<K, V>> {
    this.#statics[key] = {
      enumerable: true,
      get() {
        return value;
      },
    };

    return this as ModelBuilder<T, C & Record<K, V>>;
  }

  name(name: string): this {
    this.define('name', name);
    return this;
  }

  build(): ModelConstructor<T, C> {
    const self = this;

    const ctr = class extends self.#base {};

    Object.defineProperties(ctr, this.#statics);

    return ctr as any;
  }
}

export class ObjectModelBuilder<T, U, C extends object = EmptyRecord> implements ModelBuilder<T, C> {
  static from<T, U = object>(base: ModelConstructor): ObjectModelBuilder<T, U> {
    return new ObjectModelBuilder(base);
  }

  readonly #base: ModelConstructor;
  readonly #initializers: Array<(self: ExtendedModel<T>) => void> = [];
  readonly #properties: Record<keyof any, PropertyDescriptor> = {};
  readonly #propertyModels: Array<readonly [keyof any, ModelConstructor]> = [];
  readonly #statics: Record<keyof any, PropertyDescriptor> = {};

  private constructor(base: ModelConstructor) {
    this.#base = base;

    this.#statics.defaultValue = {
      enumerable: true,
      get: () => Object.fromEntries(this.#propertyModels.map(([key, model]) => [key, model.defaultValue] as const)),
    };
  }

  define<K extends keyof any, V>(key: K, value: V): ObjectModelBuilder<T, U, C & Record<K, V>> {
    this.#statics[key] = {
      enumerable: true,
      get() {
        return value;
      },
    };

    return this as ObjectModelBuilder<T, U, C & Record<K, V>>;
  }

  name(name: string): this {
    this.define('name', name);
    return this;
  }

  property<K extends keyof any, N>(
    key: K,
    model: ModelConstructor<N>,
    options?: ModelBuilderPropertyOptions,
  ): ObjectModelBuilder<T, U, C & Record<K, N>> {
    const registry = new WeakMap<Model, Model>();

    this.#propertyModels.push([key, model] as const);

    this.#initializers.push((self) => {
      registry.set(self, new model(key, self, options?.meta));
    });

    this.#properties[key] = {
      enumerable: true,
      get(this: Model<U>) {
        return registry.get(this);
      },
    };

    return this as ObjectModelBuilder<T, U, C & Record<K, N>>;
  }

  build(): U extends T ? ModelConstructor<T, C> : never {
    const self = this;

    const ctr = class extends self.#base {
      constructor(key: keyof any, owner: Model | ModelOwner, meta?: ModelMetadata) {
        super(key, owner, meta);
        self.#initializers.forEach((initializer) => initializer(this as ExtendedModel<T>));
      }
    };

    Object.defineProperties(ctr.prototype, this.#properties);
    Object.defineProperties(ctr, this.#statics);

    return ctr as any;
  }
}
