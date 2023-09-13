import type { AbstractModel, ModelConstructor } from '@hilla/form';

export interface PropertyInfo {
  name: string;
  humanReadableName: string;
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

export const getProperties = (model: ModelConstructor<unknown, AbstractModel<unknown>>): PropertyInfo[] => {
  const properties = Object.keys(Object.getOwnPropertyDescriptors(model.prototype)).filter((p) => p !== 'constructor');

  return properties.map((name) => {
    const humanReadableName = _generateHeader(name);

    return {
      name,
      humanReadableName,
    };
  });
};
