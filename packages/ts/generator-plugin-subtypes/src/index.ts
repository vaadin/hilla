import type { SourceFile } from '@typescript/typescript6';
import Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import {
  isReferenceSchema,
  convertFullyQualifiedNameToRelativePath,
  type ReferenceSchema,
  type Schema,
} from '@vaadin/hilla-generator-core/Schema.js';
import type { SharedStorage } from '@vaadin/hilla-generator-core/SharedStorage.js';
import type { OpenAPIV3 } from 'openapi-types';
import { ModelFixProcessor } from './ModelFixProcessor.js';
import { SubTypesProcessor } from './SubTypesProcessor.js';
import { TypeFixProcessor } from './TypeFixProcessor.js';

const SCHEMA_PREFIX = '#/components/schemas/';

// the property name used by Jackson unless `@JsonTypeInfo` specifies another
// one: kept as a fallback for OpenAPI documents without a `discriminator`
const DEFAULT_DISCRIMINATOR = '@type';

type Components = Readonly<Record<string, Schema>>;

function schemaKey(schema: ReferenceSchema): string {
  return schema.$ref.substring(SCHEMA_PREFIX.length);
}

/**
 * Returns the schemas that hold the properties declared by the type itself: a
 * subtype is a composed schema whose `anyOf` list contains a reference to the
 * supertype and an object schema with the own properties, while a type that
 * has no supertype is a plain object schema.
 */
function ownSchemas(component: Schema): readonly OpenAPIV3.SchemaObject[] {
  if (isReferenceSchema(component)) {
    return [];
  }

  return component.anyOf
    ? component.anyOf.filter((schema): schema is OpenAPIV3.SchemaObject => 'properties' in schema)
    : [component];
}

function superTypeKey(component: Schema): string | undefined {
  if (isReferenceSchema(component) || !component.anyOf) {
    return undefined;
  }

  return component.anyOf.filter(isReferenceSchema).map(schemaKey)[0];
}

/**
 * Returns the value of the discriminator property declared by the type itself,
 * which the Java parser stores as the `example` of that property.
 */
function discriminatorValue(component: Schema | undefined, propertyName: string): string | undefined {
  if (!component) {
    return undefined;
  }

  for (const schema of ownSchemas(component)) {
    const property = schema.properties?.[propertyName];

    if (property && 'example' in property && typeof property.example === 'string') {
      return property.example;
    }
  }

  return undefined;
}

function fixSubType(sources: SourceFile[], components: Components, subKey: string, propertyName: string): void {
  const typeValue = discriminatorValue(components[subKey], propertyName);

  if (typeValue === undefined) {
    return;
  }

  // a type that extends another subtype inherits a discriminator with a
  // different value, so that property has to be omitted from the supertype
  const superType = superTypeKey(components[subKey]);
  const omitInheritedProperty =
    superType !== undefined && discriminatorValue(components[superType], propertyName) !== undefined;

  const subFn = `${convertFullyQualifiedNameToRelativePath(subKey)}.ts`;
  const subSource = sources.find(({ fileName }) => fileName === subFn)!;
  // fix the source to replace the discriminator property type with a literal
  const fixedSource = new TypeFixProcessor(subSource, typeValue, propertyName, omitInheritedProperty).process();
  sources.splice(sources.indexOf(subSource), 1, fixedSource);

  // fix the model to remove the discriminator property
  const modelFn = `${convertFullyQualifiedNameToRelativePath(subKey)}Model.ts`;
  const modelSource = sources.find(({ fileName }) => fileName === modelFn)!;
  const fixedModelSource = new ModelFixProcessor(modelSource, propertyName).process();
  sources.splice(sources.indexOf(modelSource), 1, fixedModelSource);
}

export default class SubTypesPlugin extends Plugin {
  declare ['constructor']: typeof SubTypesPlugin;

  override get path(): string {
    return import.meta.url;
  }

  // eslint-disable-next-line @typescript-eslint/require-await
  override async execute(storage: SharedStorage): Promise<void> {
    const { api, sources } = storage;

    const components = api.components?.schemas;

    if (!components) {
      return;
    }

    Object.entries(components).forEach(([baseKey, baseComponent]) => {
      // search for components with oneOf: those are union types
      if (
        'oneOf' in baseComponent &&
        Array.isArray(baseComponent.oneOf) &&
        baseComponent.oneOf.every((schema) => isReferenceSchema(schema))
      ) {
        const fn = `${convertFullyQualifiedNameToRelativePath(baseKey)}.ts`;
        const source = sources.find(({ fileName }) => fileName === fn)!;
        // replace the (empty) source with a newly-generated one
        const newSource = new SubTypesProcessor(baseKey, source, baseComponent.oneOf).process();
        sources.splice(sources.indexOf(source), 1, newSource);

        // `@JsonTypeInfo` may use any property name as the discriminator
        const propertyName = baseComponent.discriminator?.propertyName ?? DEFAULT_DISCRIMINATOR;

        // mentioned types in the oneOf need to be fixed as well
        baseComponent.oneOf.forEach((schema) => {
          fixSubType(sources, components, schemaKey(schema), propertyName);
        });

        // remove the union type model file
        const unionFn = `${convertFullyQualifiedNameToRelativePath(baseKey)}Model.ts`;
        const unionSource = sources.find(({ fileName }) => fileName === unionFn)!;
        sources.splice(sources.indexOf(unionSource), 1);
      }
    });
  }
}
