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
