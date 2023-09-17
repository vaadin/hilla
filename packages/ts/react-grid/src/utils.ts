import { StringModel, type AbstractModel, type ModelConstructor, NumberModel } from '@hilla/form';

export interface PropertyInfo {
  name: string;
  humanReadableName: string;
  modelType: 'string' | 'number' | undefined;
}

// This is from vaadin-grid-column.js, should be used from there maybe. At least we must be 100% sure to match grid and fields
function _generateHeader(path: string) {
  return path
    .substring(path.lastIndexOf('.') + 1)
    .replace(/([A-Z])/gu, '-$1')
    .toLowerCase()
    .replace(/-/gu, ' ')
    .replace(/^./u, (match) => match.toUpperCase());
}

export const getProperties = (model: ModelConstructor<unknown | undefined, AbstractModel<unknown>>): PropertyInfo[] => {
  const properties = Object.keys(Object.getOwnPropertyDescriptors(model.prototype)).filter((p) => p !== 'constructor');
  const modelInstance: any = new model({ value: undefined }, '', false);
  return properties.map((name) => {
    const propertyModel = modelInstance[name];
    const humanReadableName = _generateHeader(name);
    const modelType =
      propertyModel.constructor === StringModel
        ? 'string'
        : propertyModel.constructor === NumberModel
        ? 'number'
        : undefined;
    return {
      name,
      humanReadableName,
      modelType,
    };
  });
};
