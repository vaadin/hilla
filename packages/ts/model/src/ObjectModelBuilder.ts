import exp from 'constants';
import { CoreModelBuilder } from './CoreModelBuilder.js';
import { ObjectModel, toObject, type TypeModel } from './coreModels.js';
import { _key, _meta, _name, _owner, _value, type IModel, type Value } from './Model.js';
import type { ModelMetadata } from './ModelMetadata.js';
import { ModelBuilderUtil, getValue, type ValueExtractor, type ModelWithProperty, isModel } from './utils.js';

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

export class ObjectSubtypeModelBuilder<T, M extends IModel<T>> {
  #modelBuilderUtil: ModelBuilderUtil<T, M>;

  protected constructor(superModel: IModel, valueExtractor: ValueExtractor<T, M>) {
    this.#modelBuilderUtil = new ModelBuilderUtil(superModel, valueExtractor);
  }

  peek(): M {
    return this.#modelBuilderUtil.create();
  }

  build(this: ObjectSubtypeModelBuilder<T, TypeModel<T>>): TypeModel<T> {
    return this.#modelBuilderUtil.create();
  }

  meta(meta: ModelMetadata = {}): ObjectSubtypeModelBuilder<T, M> {
    return this.#define(_meta, meta);
  }

  property<K extends string & keyof T, MValue extends IModel<T[K]>>(
    key: K,
    valueModel: MValue | ValueExtractor<MValue, TypeModel<T>>,
    options: {
      meta?: ModelMetadata;
    } = {},
  ): ObjectSubtypeModelBuilder<T, ModelWithProperty<M, K, MValue>> {
    this.#modelBuilderUtil.defineProperty(key, {
      enumerable: true,
      get(this: M & TypeModel<T>): MValue {
        if (!attachedModelRecordCache.has(this)) {
          attachedModelRecordCache.set(this, {});
        }

        const attachedModelRecord = attachedModelRecordCache.get(this)!;
        const detachedValueModel: MValue = isModel(valueModel) ? valueModel : valueModel(this);
        attachedModelRecord[key] ??= CoreModelBuilder.from(detachedValueModel)
          .define(_owner, this)
          .define(_key, key)
          .define(_meta, options.meta)
          .build();
        return attachedModelRecord[key] as MValue;
      },
    });
    return this as unknown as ObjectSubtypeModelBuilder<T, ModelWithProperty<M, K, MValue>>;
  }

  #define<K extends keyof any, V>(key: K, value: V): ObjectSubtypeModelBuilder<T, ModelWithProperty<M, K, V>> {
    this.#modelBuilderUtil.defineProperty(key, { enumerable: false, value });
    return this as ObjectSubtypeModelBuilder<T, ModelWithProperty<M, K, V>>;
  }

  static object<MSuper extends IModel, T extends Value<MSuper>>(
    superModel: MSuper,
    valueExtractor: ValueExtractor<T, IModel<T>>,
    name: string,
  ): ObjectSubtypeModelBuilder<T, TemplateModel<T, MSuper>> {
    return new ObjectSubtypeModelBuilder<T, TemplateModel<T, MSuper>>(superModel, valueExtractor).#define(_name, name);
  }
}

export class ObjectModelBuilder<MSuper extends IModel<object>> {
  #superModel: MSuper;

  private constructor(superModel: MSuper) {
    this.#superModel = superModel;
  }

  object<T extends Value<MSuper>>(name: string): ObjectSubtypeModelBuilder<T, TemplateModel<T, MSuper>> {
    return ObjectSubtypeModelBuilder.object(this.#superModel, toObject<T>, name);
  }

  static object<T extends object>(name: string): ObjectSubtypeModelBuilder<T, TemplateModel<T, typeof ObjectModel>> {
    return ObjectSubtypeModelBuilder.object(ObjectModel, toObject<T>, name);
  }

  static extend<MSuper extends IModel<object>>(superModel: MSuper): ObjectModelBuilder<MSuper> {
    return new ObjectModelBuilder<MSuper>(superModel);
  }
}
