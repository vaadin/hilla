import { AbstractModel, _value, type IModel, type Value } from './Model';

export function isModel<M extends IModel = IModel>(maybeModel: unknown, modelType?: M): maybeModel is M {
  if (typeof maybeModel !== 'object') {
    return false;
  }

  const modelTypeForComparison = modelType ?? AbstractModel;
  if (maybeModel === modelTypeForComparison) {
    return true;
  }

  return Object.prototype.isPrototypeOf.call(modelTypeForComparison, maybeModel!);
}

export function getValue<M extends IModel>(model: M): Value<M> {
  return model[_value] as Value<M>;
}

export type ValueExtractor<T, M extends IModel = IModel> = (self: M) => T;

export type ModelWithProperty<M extends IModel, K extends keyof any, V = unknown> = M & { readonly [Key in K]: V };

export type ModelPropertyDescriptor<M extends IModel, V = unknown> = Readonly<
  {
    enumerable: boolean;
  } & ({ get(this: M): V } | { value: V })
>;

export class ModelBuilderUtil<T, M extends IModel<T>> {
  #superModel: IModel;

  #descriptorMap: Record<keyof any, ModelPropertyDescriptor<M>> = {};

  constructor(superModel: IModel, valueExtractor: ValueExtractor<T, M>) {
    this.#superModel = superModel;
    this.#descriptorMap[_value] = {
      enumerable: false,
      get() {
        return valueExtractor(this);
      },
    };
  }

  create(): M {
    return Object.create(this.#superModel, this.#descriptorMap);
  }

  defineProperty<K extends keyof any, V>(key: K, descriptor: ModelPropertyDescriptor<M, V>): void {
    this.#descriptorMap[key] = descriptor;
  }
}
