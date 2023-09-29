import {
  BooleanModel,
  NumberModel,
  StringModel,
  _meta,
  createDetachedModel,
  type AbstractModel,
  type DetachedModelConstructor,
  type ModelMetadata,
} from '@hilla/form';

export type PropertyType = 'boolean' | 'date' | 'datetime' | 'number' | 'string' | 'time' | undefined;

const javaTypeMap: Record<string, PropertyType> = {
  'java.util.Date': 'date',
  'java.time.LocalDate': 'date',
  'java.time.LocalTime': 'time',
  'java.time.LocalDateTime': 'datetime',
};

function determinePropertyType(model: AbstractModel) {
  // Try detecting by Java type
  const { javaType } = model[_meta];
  const propertyType = javaType ? javaTypeMap[javaType] : undefined;
  if (propertyType) {
    return propertyType;
  }

  // Otherwise detect by model constructor
  const { constructor } = model;
  return constructor === StringModel
    ? 'string'
    : constructor === NumberModel
    ? 'number'
    : constructor === BooleanModel
    ? 'boolean'
    : undefined;
}

export interface PropertyInfo {
  name: string;
  humanReadableName: string;
  type: PropertyType;
  meta: ModelMetadata;
}

export function hasAnnotation(propertyInfo: PropertyInfo, annotationName: string): boolean {
  return propertyInfo.meta.annotations?.some((annotation) => annotation.name === annotationName) ?? false;
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

export const getProperties = (model: DetachedModelConstructor<AbstractModel>): PropertyInfo[] => {
  const properties = Object.keys(Object.getOwnPropertyDescriptors(model.prototype)).filter((p) => p !== 'constructor');
  // eslint-disable-next-line @typescript-eslint/no-unsafe-call
  const modelInstance: any = createDetachedModel(model);
  return properties.map((name) => {
    // eslint-disable-next-line
    const propertyModel = modelInstance[name] as AbstractModel;
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    const meta = propertyModel[_meta];
    const humanReadableName = _generateHeader(name);
    const { constructor } = propertyModel;
    const type = determinePropertyType(propertyModel);
    return {
      name,
      humanReadableName,
      type,
      meta,
    };
  });
};
