import {
  BooleanModel,
  NumberModel,
  StringModel,
  _meta,
  createDetachedModel,
  type AbstractModel,
  type DetachedModelConstructor,
  type ModelMetadata,
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
  | 'object'
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
  } else if (model instanceof ObjectModel) {
    return 'object';
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

const getPropertyNames = (model: DetachedModelConstructor<AbstractModel>): string[] => {
  const propertyNames: string[] = [];

  for (let proto = model; proto !== ObjectModel; proto = Object.getPrototypeOf(proto)) {
    // parent properties are added at the beginning
    propertyNames.unshift(...Object.keys(Object.getOwnPropertyDescriptors(proto.prototype)).filter((p) => p !== 'new'));
  }

  return propertyNames;
};

export class ModelInfo {
  private readonly modelInstance: AbstractModel;

  readonly idProperty?: PropertyInfo;

  constructor(model: DetachedModelConstructor<AbstractModel>, idPropertyName?: string) {
    this.modelInstance = createDetachedModel(model);

    // Try to find id property
    this.idProperty = ModelInfo.resolveIdProperty(this, idPropertyName);
  }

  private static resolveIdProperty(modelInfo: ModelInfo, idPropertyName?: string): PropertyInfo | undefined {
    // Return explicit property if defined
    if (idPropertyName) {
      return modelInfo.getProperty(idPropertyName);
    }

    // Otherwise check defaults
    const rootProperties = modelInfo.getRootProperties();
    // Check for @Id annotation
    let idProperty = rootProperties.find((propertyInfo) => hasAnnotation(propertyInfo.meta, 'jakarta.persistence.Id'));
    // Check for id name as fallback
    if (!idProperty) {
      idProperty = rootProperties.find((propertyInfo) => propertyInfo.name === 'id');
    }

    return idProperty;
  }

  private static resolvePropertyModel(modelInstance: AbstractModel, path: string): AbstractModel | undefined {
    const parts = path.split('.');
    let currentModel: AbstractModel | undefined = modelInstance;
    for (const part of parts) {
      if (!currentModel || !(currentModel instanceof ObjectModel)) {
        return undefined;
      }
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      currentModel = (currentModel as any)[part];
    }
    return currentModel;
  }

  getRootProperties(path?: string): PropertyInfo[] {
    const model = path ? ModelInfo.resolvePropertyModel(this.modelInstance, path) : this.modelInstance;
    if (!model) {
      return [];
    }
    return getPropertyNames(model.constructor as any)
      .map((name) => {
        const effectivePath = path ? `${path}.${name}` : name;
        return this.getProperty(effectivePath);
      })
      .filter(Boolean) as PropertyInfo[];
  }

  getProperty(path: string): PropertyInfo | undefined {
    const propertyModel = ModelInfo.resolvePropertyModel(this.modelInstance, path);
    if (!propertyModel?.[_meta]) {
      return undefined;
    }

    const pathParts = path.split('.');
    const name = pathParts[pathParts.length - 1];

    const meta = propertyModel[_meta];
    const humanReadableName = _generateHeader(name);
    const type = determinePropertyType(propertyModel);

    return {
      name: path,
      humanReadableName,
      type,
      meta,
      model: propertyModel,
    };
  }

  getProperties(paths: string[]): PropertyInfo[] {
    return paths.map((path) => this.getProperty(path)).filter(Boolean) as PropertyInfo[];
  }
}

export function getDefaultProperties(modelInfo: ModelInfo): PropertyInfo[] {
  // Start from root properties
  const properties = modelInfo.getRootProperties();
  return (
    properties
      // Auto-expand nested properties of one-to-one relations
      .flatMap((prop) => {
        if (hasAnnotation(prop.meta, 'jakarta.persistence.OneToOne')) {
          return modelInfo.getRootProperties(prop.name);
        }
        return prop;
      })
      // Exclude properties that have an unknown type, or are annotated with id
      // and version
      .filter(
        (prop) =>
          !!prop.type &&
          !(
            hasAnnotation(prop.meta, 'jakarta.persistence.Id') ||
            hasAnnotation(prop.meta, 'jakarta.persistence.Version')
          ),
      )
  );
}
