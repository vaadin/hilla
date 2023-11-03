import { _value, type IModel, _meta, _name, _owner, _key, type Value } from './Model.js';
import { ModelBuilderUtil, type ModelWithProperty, type ValueExtractor } from './utils.js';

interface ICoreModelBuilder<T, M extends IModel<T>> {
  define<K extends symbol, V>(key: K, value: V): ICoreModelBuilder<T, ModelWithProperty<M, K, V>>;
  name(name: string): ICoreModelBuilder<T, M>;
  build(): M;
}

export class CoreModelBuilder<T, M extends IModel<T>> implements ICoreModelBuilder<T, M> {
  #modelBuilderUtil: ModelBuilderUtil<T, M>;

  private constructor(superModel: IModel, valueExtractor: ValueExtractor<T, M>) {
    this.#modelBuilderUtil = new ModelBuilderUtil(superModel, valueExtractor);
  }

  define<K extends symbol, V>(key: K, value: V): ICoreModelBuilder<T, ModelWithProperty<M, K, V>> {
    this.#modelBuilderUtil.defineProperty(key, { enumerable: false, value });
    return this as ICoreModelBuilder<T, ModelWithProperty<M, K, V>>;
  }

  name(name: string): ICoreModelBuilder<T, M> {
    return this.define(_name, name);
  }

  build(): M {
    return this.#modelBuilderUtil.create();
  }

  static from<MSuper extends IModel, T extends Value<MSuper> = Value<MSuper>>(
    superModel: MSuper,
    valueExtractor?: ValueExtractor<T, IModel<T> & MSuper>,
  ): ICoreModelBuilder<T, IModel<T>> {
    return new CoreModelBuilder(superModel, valueExtractor ?? (() => superModel[_value]));
  }
}
