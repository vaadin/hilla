import type { SourceFile } from '@typescript/typescript6';
import Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import {
  isReferenceSchema,
  convertFullyQualifiedNameToRelativePath,
  type Schema,
} from '@vaadin/hilla-generator-core/Schema.js';
import type { SharedStorage } from '@vaadin/hilla-generator-core/SharedStorage.js';
import type { OpenAPIV3 } from 'openapi-types';
import { ModelFixProcessor } from './ModelFixProcessor.js';
import { SubTypesProcessor } from './SubTypesProcessor.js';
import { TypeFixProcessor } from './TypeFixProcessor.js';
import type { Discriminator } from './utils.js';
import { schemaKey } from './utils.js';

// the property name used by Jackson unless `@JsonTypeInfo` specifies another
// one: kept as a fallback for OpenAPI documents without a `discriminator`
const DEFAULT_DISCRIMINATOR = '@type';

type Components = Readonly<Record<string, Schema>>;

/**
 * Returns the schemas that hold the properties declared by the type itself: a
 * subtype is a composed schema whose `anyOf` list contains a reference to the
 * supertype and an object schema with the own properties, while a type that has
 * no supertype is a plain object schema.
 */
function ownSchemas(component: Schema): readonly OpenAPIV3.SchemaObject[] {
  if (isReferenceSchema(component)) {
    return [];
  }

  return component.anyOf
    ? component.anyOf.filter((schema): schema is OpenAPIV3.SchemaObject => 'properties' in schema)
    : [component];
}

/**
 * Returns the values that the discriminator property of the type accepts: the
 * Java parser stores them as the `enum` of that property, the own value of the
 * type first, followed by the values of the subtypes below it, which narrow the
 * discriminator further. The `example`, which holds the own value alone, is the
 * fallback for documents without an `enum`.
 */
function discriminatorValues(
  component: Schema | undefined,
  discriminatorPropertyName: string,
): readonly string[] | undefined {
  if (!component) {
    return undefined;
  }

  for (const schema of ownSchemas(component)) {
    const property = schema.properties?.[discriminatorPropertyName];

    if (property && 'enum' in property && property.enum?.length) {
      return property.enum.filter((value): value is string => typeof value === 'string');
    }

    if (property && 'example' in property && typeof property.example === 'string') {
      return [property.example];
    }
  }

  return undefined;
}

/**
 * Returns the values that the discriminator accepts in each subtype, mapped by
 * schema name. A subtype that is also the supertype of another subtype accepts
 * more than one value: pinning it to its own value would leave the subtype
 * unable to extend it, or to be used as the type parameter of its model, so the
 * union type pins the own value of such a subtype instead.
 */
function findDiscriminatorValues(
  components: Components,
  keys: readonly string[],
  discriminatorPropertyName: string,
): Map<string, readonly string[]> {
  const values = new Map<string, readonly string[]>();

  keys.forEach((key) => {
    const subTypeValues = discriminatorValues(components[key], discriminatorPropertyName);

    if (subTypeValues?.length) {
      values.set(key, subTypeValues);
    }
  });

  return values;
}

function fixSubType(sources: SourceFile[], subKey: string, discriminator: Discriminator): void {
  const { propertyName: discriminatorPropertyName, values } = discriminator;
  const typeValues = values.get(subKey);

  if (!typeValues) {
    return;
  }

  const subFn = `${convertFullyQualifiedNameToRelativePath(subKey)}.ts`;
  const subSource = sources.find(({ fileName }) => fileName === subFn)!;
  // fix the source to turn the discriminator property into the string literals
  // that the type accepts
  const fixedSource = new TypeFixProcessor(subSource, discriminatorPropertyName, typeValues).process();
  sources.splice(sources.indexOf(subSource), 1, fixedSource);

  // fix the model to remove the discriminator property
  const modelFn = `${convertFullyQualifiedNameToRelativePath(subKey)}Model.ts`;
  const modelSource = sources.find(({ fileName }) => fileName === modelFn)!;
  const fixedModelSource = new ModelFixProcessor(modelSource, discriminatorPropertyName).process();
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
        // `@JsonTypeInfo` may use any property name as the discriminator
        const discriminatorPropertyName = baseComponent.discriminator?.propertyName ?? DEFAULT_DISCRIMINATOR;
        const subKeys = baseComponent.oneOf.map(schemaKey);
        const discriminator = {
          propertyName: discriminatorPropertyName,
          values: findDiscriminatorValues(components, subKeys, discriminatorPropertyName),
        };

        const fn = `${convertFullyQualifiedNameToRelativePath(baseKey)}.ts`;
        const source = sources.find(({ fileName }) => fileName === fn)!;
        // replace the (empty) source with a newly-generated one
        const newSource = new SubTypesProcessor(baseKey, source, baseComponent.oneOf, discriminator).process();
        sources.splice(sources.indexOf(source), 1, newSource);

        // mentioned types in the oneOf need to be fixed as well
        subKeys.forEach((subKey) => {
          fixSubType(sources, subKey, discriminator);
        });

        // remove the union type model file
        const unionFn = `${convertFullyQualifiedNameToRelativePath(baseKey)}Model.ts`;
        const unionSource = sources.find(({ fileName }) => fileName === unionFn)!;
        sources.splice(sources.indexOf(unionSource), 1);
      }
    });
  }
}
