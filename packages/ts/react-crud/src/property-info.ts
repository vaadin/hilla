import {
  BooleanModel,
  NumberModel,
  StringModel,
  _meta,
  createDetachedModel,
  type AbstractModel,
  type DetachedModelConstructor,
  type ModelMetadata,
  _enum,
  EnumModel,
  ObjectModel,
} from '@hilla/form';

export type PropertyType =
  | 'boolean'
  | 'date'
  | 'datetime'
  | 'decimal'
  | 'enum'
  | 'integer'
  | 'string'
  | 'time'
  | undefined;

const javaTypeMap: Record<string, PropertyType> = {
  byte: 'integer',
  'java.lang.Byte': 'integer',
  short: 'integer',
  'java.lang.Short': 'integer',
  int: 'integer',
  'java.lang.Integer': 'integer',
  long: 'integer',
  'java.lang.Long': 'integer',
  float: 'decimal',
  'java.lang.Float': 'decimal',
  double: 'decimal',
  'java.lang.Double': 'decimal',
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

  // Otherwise detect by model instance
  if (model instanceof StringModel) {
    return 'string';
  } else if (model instanceof NumberModel) {
    return 'decimal';
  } else if (model instanceof BooleanModel) {
    return 'boolean';
  } else if (model instanceof EnumModel) {
    return 'enum';
  }

  return undefined;
}

export interface PropertyInfo {
  name: string;
  humanReadableName: string;
  type: PropertyType;
  meta: ModelMetadata;
  model: AbstractModel;
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

export const getPropertyIds = (model: DetachedModelConstructor<AbstractModel>): string[] => {
  const propertyIds: string[] = [];

  for (let proto = model; proto !== ObjectModel; proto = Object.getPrototypeOf(proto)) {
    // parent properties are added at the beginning
    propertyIds.unshift(...Object.keys(Object.getOwnPropertyDescriptors(proto.prototype)).filter((p) => p !== 'new'));
  }

  return propertyIds;
};

export const getProperties = (model: DetachedModelConstructor<AbstractModel>): PropertyInfo[] => {
  const propertyIds = getPropertyIds(model);
  // eslint-disable-next-line @typescript-eslint/no-unsafe-call
  const modelInstance: any = createDetachedModel(model);
  return (
    propertyIds
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      .map((name) => [name, modelInstance[name]])
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      .filter(([, m]) => m?.[_meta])
      .flatMap(([name, m]) => {
        const propertyModel = m as AbstractModel;
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
          model: propertyModel,
        };
      })
  );
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
