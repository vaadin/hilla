import {
  AbstractModel,
  BooleanModel as BinderBooleanModel,
  NumberModel as BinderNumberModel,
  StringModel as BinderStringModel,
  _meta,
  createDetachedModel,
  type DetachedModelConstructor,
  EnumModel as BinderEnumModel,
  ObjectModel as BinderObjectModel,
  type ProvisionalModel,
} from '@vaadin/hilla-lit-form';
import {
  type Annotation,
  type AnnotationValue,
  BooleanModel,
  EnumModel,
  Model,
  type ModelMetadata,
  NumberModel,
  ObjectModel,
  StringModel,
} from '@vaadin/hilla-models';

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
  'java.time.OffsetTime': 'time',
  'java.time.LocalDateTime': 'datetime',
  'java.time.OffsetDateTime': 'datetime',
  'java.time.ZonedDateTime': 'datetime',
  'java.util.Date': 'datetime',
  'java.sql.Date': 'datetime',
};

function determinePropertyType(model: ProvisionalModel): PropertyType {
  // Try detecting by Java type
  const javaType = model instanceof AbstractModel ? model[_meta].javaType : (model as Model)[_meta]?.jvmType;
  const propertyType = javaType ? javaTypeMap[javaType] : undefined;
  if (propertyType) {
    return propertyType;
  }

  // Otherwise detect by model instance
  if (model instanceof BinderStringModel || model === StringModel || model instanceof StringModel) {
    return 'string';
  } else if (model instanceof BinderNumberModel || model === NumberModel || model instanceof NumberModel) {
    return 'decimal';
  } else if (model instanceof BinderBooleanModel || model === BooleanModel || model instanceof BooleanModel) {
    return 'boolean';
  } else if (model instanceof BinderEnumModel || model instanceof EnumModel || model instanceof EnumModel) {
    return 'enum';
  } else if (model instanceof BinderObjectModel || model === ObjectModel || model instanceof ObjectModel) {
    return 'object';
  }

  return undefined;
}

export interface PropertyInfo {
  name: string;
  humanReadableName: string;
  type: PropertyType;
  meta: ModelMetadata;
  model: ProvisionalModel;
}

export function hasAnnotation(meta: ModelMetadata, annotationName: string): boolean {
  return meta.annotations?.some((annotation) => annotation.jvmType === annotationName) ?? false;
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

const getPropertyNames = (model: ProvisionalModel): string[] => {
  const propertyNames: string[] = [];

  if (model instanceof AbstractModel) {
    const modelClass = model.constructor as unknown as DetachedModelConstructor<AbstractModel>;
    for (let proto = modelClass; proto !== BinderObjectModel; proto = Object.getPrototypeOf(proto)) {
      // skip `constructor`, take only own properties with getters or value
      const descriptorEntries = Object.entries(Object.getOwnPropertyDescriptors(proto.prototype)).filter(
        ([_name, propertyDescriptor]) =>
          Object.hasOwn(propertyDescriptor, 'get') || !(propertyDescriptor.value instanceof Function),
      );
      // parent properties are added at the beginning
      propertyNames.unshift(...descriptorEntries.map(([name]) => name));
    }
  } else {
    for (let proto = model; proto !== ObjectModel; proto = Object.getPrototypeOf(proto)) {
      propertyNames.unshift(...Object.getOwnPropertyNames(proto));
    }
  }

  return propertyNames;
};

export class ModelInfo {
  private readonly modelInstance: ProvisionalModel;

  readonly idProperty?: PropertyInfo;

  constructor(model: DetachedModelConstructor<AbstractModel> | Model, idPropertyName?: string) {
    const isModel = model === Model || model instanceof Model;
    this.modelInstance = isModel ? model : createDetachedModel(model as DetachedModelConstructor<AbstractModel>);

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
    idProperty ??= rootProperties.find((propertyInfo) => propertyInfo.name === 'id');

    return idProperty;
  }

  private static resolvePropertyModel(modelInstance: ProvisionalModel, path: string): ProvisionalModel | undefined {
    const parts = path.split('.');
    let currentModel: ProvisionalModel | undefined = modelInstance;
    for (const part of parts) {
      if (!currentModel || !(currentModel instanceof BinderObjectModel || currentModel instanceof ObjectModel)) {
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
    return getPropertyNames(model)
      .map((name) => {
        const effectivePath = path ? `${path}.${name}` : name;
        return this.getProperty(effectivePath);
      })
      .filter(Boolean) as PropertyInfo[];
  }

  getProperty(path: string): PropertyInfo | undefined {
    const propertyModel = ModelInfo.resolvePropertyModel(this.modelInstance, path);
    if (!propertyModel) {
      return undefined;
    }

    const pathParts = path.split('.');
    const name = pathParts[pathParts.length - 1];

    const meta =
      propertyModel instanceof AbstractModel
        ? ({
            jvmType: propertyModel[_meta].javaType,
            annotations: propertyModel[_meta].annotations?.map(
              (annotation) =>
                ({
                  jvmType: annotation.name,
                  attributes: annotation.attributes as Record<string, AnnotationValue>,
                }) satisfies Annotation,
            ),
          } satisfies ModelMetadata)
        : (propertyModel[_meta] ?? {});
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
