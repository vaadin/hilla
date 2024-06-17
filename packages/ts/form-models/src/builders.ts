import type { ObjectModel } from './core';
import {
  $defaultValue,
  $key,
  $meta,
  $name,
  $owner,
  type DefaultValueProvider,
  type EmptyRecord,
  type Model,
  type ModelMetadata,
} from './model.js';

const { create, defineProperty, fromEntries, entries } = Object;

export type ModelBuilderPropertyOptions = Readonly<{
  meta?: ModelMetadata;
}>;

const $model = Symbol();

declare const $named: unique symbol;

export type NamedModelBuilder = Readonly<{ [$named]: true }>;

export class CoreModelBuilder<T, C extends object = EmptyRecord, N extends boolean = false> {
  declare readonly [$named]: N;
  protected readonly [$model]: C & Model<T>;

  constructor(base: Model, defaultValueProvider?: DefaultValueProvider<T, C>) {
    this[$model] = create(base);

    if (defaultValueProvider) {
      defineProperty(this[$model], $defaultValue, {
        get(this: C & Model<T>) {
          return defaultValueProvider(this);
        },
      });
    }
  }

  meta(value: ModelMetadata): this {
    this.define($meta, value);
    return this;
  }

  define<K extends symbol, V>(key: K, value: V): CoreModelBuilder<T, C & Readonly<Record<K, V>>, N> {
    defineProperty(this[$model], key, { value });
    return this as CoreModelBuilder<T, C & Readonly<Record<K, V>>, N>;
  }

  name(name: string): CoreModelBuilder<T, C, true> {
    return this.define($name, name) as CoreModelBuilder<T, C, true>;
  }

  build(): this extends NamedModelBuilder ? C & Model<T> : never {
    return this[$model] as this extends NamedModelBuilder ? C & Model<T> : never;
  }
}

export type ModelProvider<T extends object = Record<keyof any, unknown>, K extends keyof T = keyof T> = (
  model: Model<T>,
) => Model<T[K]>;

export class ObjectModelBuilder<
  T extends object,
  U extends object = object,
  C extends object = EmptyRecord,
  N extends boolean = false,
> extends CoreModelBuilder<T, C, N> {
  constructor(base: Model) {
    super(
      base,
      (m) =>
        fromEntries(
          (entries(m) as ReadonlyArray<readonly [string, Model<T[keyof T]>]>).map(([key, child]) => [
            key,
            child[$defaultValue],
          ]),
        ) as ReturnType<DefaultValueProvider<T, C>>,
    );
  }

  declare ['build']: () => this extends NamedModelBuilder ? (U extends T ? C & ObjectModel<T> : never) : never;

  declare ['define']: <K extends symbol, V>(
    key: K,
    value: V,
  ) => ObjectModelBuilder<T, U, C & Readonly<Record<K, V>>, N>;

  declare ['name']: <NT extends object>(name: string) => ObjectModelBuilder<NT, U, C, true>;

  declare ['meta']: (value: ModelMetadata) => this;

  property<K extends keyof T>(
    key: K,
    model: Model<T[K]> | ModelProvider<T, K>,
    options?: ModelBuilderPropertyOptions,
  ): ObjectModelBuilder<T, Readonly<Record<K, T[K]>> & U, C, N> {
    defineProperty(this[$model], key, {
      enumerable: true,
      value: new CoreModelBuilder<T[K]>(typeof model === 'function' ? model(this[$model]) : model)
        .define($key, key)
        .define($owner, this[$model])
        .define($meta, options?.meta)
        .build(),
    });

    return this as ObjectModelBuilder<T, Readonly<Record<K, T[K]>> & U, C, N>;
  }
}
