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

function determinePropertyType(model: AbstractModel): PropertyType {
  // Try detecting by Java type
  const { javaType } = model[_meta];
  const propertyType = javaType ? javaTypeMap[javaType] : undefined;
  if (propertyType) {
    return propertyType;
  }

  // Otherwise detect by model constructor
  const { constructor } = model;
  if (constructor === StringModel) {
    return 'string';
  } else if (constructor === NumberModel) {
    return 'number';
  } else if (constructor === BooleanModel) {
    return 'boolean';
  }
  return undefined;
}

export interface PropertyInfo {
  name: string;
  humanReadableName: string;
  type: PropertyType;
  meta: ModelMetadata;
}

export function hasAnnotation(meta: ModelMetadata, annotationName: string): boolean {
  return meta.annotations?.some((annotation) => annotation.name === annotationName) ?? false;
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

export const getPropertyIds = (model: DetachedModelConstructor<AbstractModel>): string[] =>
  Object.keys(Object.getOwnPropertyDescriptors(model.prototype)).filter((p) => p !== 'constructor');

export const getProperties = (model: DetachedModelConstructor<AbstractModel>): PropertyInfo[] => {
  const propertyIds = getPropertyIds(model);
  // eslint-disable-next-line @typescript-eslint/no-unsafe-call
  const modelInstance: any = createDetachedModel(model);
  return propertyIds.flatMap((name) => {
    // eslint-disable-next-line
    const propertyModel = modelInstance[name] as AbstractModel;
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    const meta = propertyModel[_meta];
    const humanReadableName = _generateHeader(name);
    const type = determinePropertyType(propertyModel);

    if (hasAnnotation(meta, 'jakarta.persistence.OneToOne')) {
      // Expand sub properties
      const subProps = getProperties(propertyModel.constructor as any);
      return subProps.map((prop) => ({ ...prop, name: `${name}.${prop.name}` }));
    }

    return {
      name,
      humanReadableName,
      type,
      meta,
    };
  });
};

export function includeProperty(propertyInfo: PropertyInfo): unknown {
  // Exclude properties annotated with id and version
  if (
    hasAnnotation(propertyInfo.meta, 'jakarta.persistence.Id') ||
    hasAnnotation(propertyInfo.meta, 'jakarta.persistence.Version')
  ) {
    return false;
  }
  if (!propertyInfo.type) {
    // Do not render columns we do not know how to render
    return false;
  }
  return true;
}

export function getIdProperty(properties: PropertyInfo[]): PropertyInfo | undefined {
  const idProperty = properties.find((propertyInfo) => hasAnnotation(propertyInfo.meta, 'jakarta.persistence.Id'));
  if (idProperty) {
    return idProperty;
  }

  return undefined;
}
