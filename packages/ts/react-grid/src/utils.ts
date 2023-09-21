import { StringModel, type AbstractModel, type ModelConstructor, NumberModel } from '@hilla/form';

export interface PropertyInfo {
  name: string;
  humanReadableName: string;
  modelType: 'number' | 'string' | undefined;
}

// This is from vaadin-grid-column.js, should be used from there maybe. At least we must be 100% sure to match grid and fields
export function _generateHeader(path: string): string {
  return path
    .substring(path.lastIndexOf('.') + 1)
    .replace(/([A-Z])/gu, '-$1')
    .toLowerCase()
    .replace(/-/gu, ' ')
    .replace(/^./u, (match) => match.toUpperCase());
}

export const getProperties = (model: ModelConstructor<unknown, AbstractModel<unknown>>): PropertyInfo[] => {
  const properties = Object.keys(Object.getOwnPropertyDescriptors(model.prototype)).filter((p) => p !== 'constructor');
  const modelInstance: any = new model({ value: undefined }, '', false);
  return properties.map((name) => {
    // eslint-disable-next-line
    const propertyModel = modelInstance[name];
    const humanReadableName = _generateHeader(name);
    const { constructor } = propertyModel;
    const modelType = constructor === StringModel ? 'string' : constructor === NumberModel ? 'number' : undefined;
    return {
      name,
      humanReadableName,
      modelType,
    };
  });
};
