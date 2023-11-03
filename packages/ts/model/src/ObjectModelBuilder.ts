import { CoreModelBuilder } from './CoreModelBuilder.js';
import type { TypeModel } from './coreModels.js';
import { _key, _meta, _name, _owner, _value, type IModel, type Value } from './Model.js';
import type { ModelMetadata } from './ModelMetadata.js';
import { ModelBuilderUtil, getValue, type ValueExtractor, type ModelWithProperty } from './utils.js';

export type TemplateModelKeys<T, M extends IModel = IModel> = T extends unknown[]
  ? never
  : T extends object
  ? Exclude<string & keyof T, string & keyof M>
  : never;

export type TemplateModel<T, M extends IModel = IModel> = IModel<T> &
  M & {
    readonly [Key in TemplateModelKeys<T, M>]?: TypeModel<T[Key]>;
  };

type Copy<T> = {
  [P in keyof T]: T[P];
};

const attachedModelRecordCache = new WeakMap<IModel, Record<keyof any, IModel | undefined>>();

export class ObjectModelBuilder<T, M extends IModel<T>> {
  #modelBuilderUtil: ModelBuilderUtil<T, M>;

  protected constructor(superModel: IModel, valueExtractor: ValueExtractor<T, M>) {
    this.#modelBuilderUtil = new ModelBuilderUtil(superModel, valueExtractor);
  }

  peek(): M {
    return this.#modelBuilderUtil.create();
  }

  build(this: ObjectModelBuilder<T, TypeModel<T>>): TypeModel<T> {
    return this.#modelBuilderUtil.create();
  }

  name(name: string): ObjectModelBuilder<T, M> {
    return this.#define(_name, name);
  }

  meta(meta: ModelMetadata = {}): ObjectModelBuilder<T, M> {
    return this.#define(_meta, meta);
  }

  property<K extends string & keyof T, MValue extends IModel<T[K]>>(
    key: K,
    valueExtractor: ValueExtractor<MValue, TypeModel<T>>,
    options: {
      meta?: ModelMetadata;
    } = {},
  ): ObjectModelBuilder<T, ModelWithProperty<M, K, MValue>> {
    this.#modelBuilderUtil.defineProperty(key, {
      enumerable: true,
      get(this: M & TypeModel<T>): MValue {
        if (!attachedModelRecordCache.has(this)) {
          attachedModelRecordCache.set(this, {});
        }

        const attachedModelRecord = attachedModelRecordCache.get(this)!;
        attachedModelRecord[key] ??= CoreModelBuilder.from(valueExtractor(this))
          .define(_owner, this)
          .define(_key, key)
          .define(_meta, options.meta)
          .build();
        return attachedModelRecord[key] as MValue;
      },
    });
    return this as unknown as ObjectModelBuilder<T, ModelWithProperty<M, K, MValue>>;
  }

  #define<K extends keyof any, V>(key: K, value: V): ObjectModelBuilder<T, ModelWithProperty<M, K, V>> {
    this.#modelBuilderUtil.defineProperty(key, { enumerable: false, value });
    return this as ObjectModelBuilder<T, ModelWithProperty<M, K, V>>;
  }

  static from<MSuper extends IModel, T extends Value<MSuper> = Value<MSuper>>(
    superModel: MSuper,
    valueExtractor: ValueExtractor<T, IModel<T> & MSuper>,
  ): ObjectModelBuilder<T, TemplateModel<T, MSuper>> {
    return new ObjectModelBuilder(superModel, valueExtractor) as any;
  }
}
