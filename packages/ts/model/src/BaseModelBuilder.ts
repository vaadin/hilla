import { _value, type IModel, _meta, _name, _owner, _key, type Value } from './Model.js';
import type { ModelMetadata } from './ModelMetadata.js';

export type ValueGetter<T, M extends IModel = IModel> = (this: M) => T;

export type IModelWithProperty<M extends IModel, K extends keyof any, V = unknown> = M & { readonly [Key in K]: V };

interface IModelBuilder<M extends IModel> {
  build(): M;
  define<K extends symbol, V>(key: K, value: V): IModelBuilder<IModelWithProperty<M, K, V>>;
  meta(meta?: ModelMetadata): IModelBuilder<M>;
  name(name: string): IModelBuilder<M>;
  property<K extends string, MValue extends IModel>(
    key: K,
    value: MValue,
    options?: {
      meta: ModelMetadata;
    },
  ): IModelBuilder<IModelWithProperty<M, K, MValue>>;
}

const attachedModelRecordCache = new WeakMap<IModel, Record<keyof any, IModel | undefined>>();

export class BaseModelBuilder<T, M extends IModel<T>> implements IModelBuilder<M> {
  #superModel: IModel;

  #propertyDescriptorMap: Record<
    keyof any,
    {
      enumerable: boolean;
    } & (
      | {
          get(this: M): unknown;
        }
      | {
          value: unknown;
        }
    )
  > = {};

  protected constructor(superModel: IModel, valueGetter: ValueGetter<T, M>) {
    this.#superModel = superModel;
    this.#propertyDescriptorMap[_value] = {
      enumerable: false,
      get() {
        return valueGetter.call(this);
      },
    };
  }

  build(): M {
    return Object.create(this.#superModel, this.#propertyDescriptorMap);
  }

  define<K extends symbol, V>(key: K, value: V): IModelBuilder<IModelWithProperty<M, K, V>> {
    this.#propertyDescriptorMap[key] = { enumerable: false, value };
    return this as IModelBuilder<IModelWithProperty<M, K, V>>;
  }

  meta(meta: ModelMetadata = {}): IModelBuilder<IModelWithProperty<M, typeof _meta, ModelMetadata>> {
    return this.define(_meta, meta);
  }

  name(name: string): IModelBuilder<IModelWithProperty<M, typeof _name, string>> {
    return this.define(_name, name);
  }

  property<K extends string, MValue extends IModel>(
    key: K,
    value: MValue,
    options: {
      meta?: ModelMetadata;
    } = {},
  ): IModelBuilder<IModelWithProperty<M, K, MValue>> {
    this.#propertyDescriptorMap[key] = {
      enumerable: true,
      get() {
        if (!attachedModelRecordCache.has(this)) {
          attachedModelRecordCache.set(this, {});
        }

        const attachedModelRecord = attachedModelRecordCache.get(this)!;
        attachedModelRecord[key] ??= BaseModelBuilder.from(value)
          .define(_owner, this)
          .define(_key, key)
          .meta(options.meta)
          .build();
        return attachedModelRecord[key];
      },
    };
    return this as unknown as IModelBuilder<IModelWithProperty<M, K, MValue>>;
  }

  static from<MSuper extends IModel, T extends Value<MSuper> = Value<MSuper>>(
    superModel: MSuper,
    valueGetter?: ValueGetter<T, MSuper>,
  ): IModelBuilder<IModel<T> & MSuper> {
    return new BaseModelBuilder(superModel, valueGetter ?? (() => superModel[_value]));
  }
}
