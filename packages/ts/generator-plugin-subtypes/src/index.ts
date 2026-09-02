import Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { isReferenceSchema, convertFullyQualifiedNameToRelativePath } from '@vaadin/hilla-generator-core/Schema.js';
import type { SharedStorage } from '@vaadin/hilla-generator-core/SharedStorage.js';
import { ModelFixProcessor } from './ModelFixProcessor.js';
import { SubTypesProcessor } from './SubTypesProcessor.js';
import { TypeFixProcessor } from './TypeFixProcessor.js';

// the property name used by Jackson unless `@JsonTypeInfo` specifies another
// one: kept as a fallback for OpenAPI documents without a `discriminator`
const DEFAULT_DISCRIMINATOR = '@type';

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
        const discriminatorPropertyName = baseComponent.discriminator?.propertyName ?? DEFAULT_DISCRIMINATOR;

        // mentioned types in the oneOf need to be fixed as well
        baseComponent.oneOf.forEach((schema) => {
          if ('$ref' in schema) {
            const path = schema.$ref;
            Object.entries(components).forEach(([subKey, subComponent]) => {
              if ('anyOf' in subComponent && subKey === path.substring('#/components/schemas/'.length)) {
                subComponent.anyOf?.forEach((s) => {
                  const property = 'properties' in s ? s.properties?.[discriminatorPropertyName] : undefined;

                  if (property && 'example' in property) {
                    const typeValue = property.example as string;
                    const subFn = `${convertFullyQualifiedNameToRelativePath(subKey)}.ts`;
                    const subSource = sources.find(({ fileName }) => fileName === subFn)!;
                    // fix the source to replace the discriminator property type
                    // with a string literal
                    const fixedSource = new TypeFixProcessor(subSource, discriminatorPropertyName, typeValue).process();
                    sources.splice(sources.indexOf(subSource), 1, fixedSource);

                    // fix the model to remove the discriminator property
                    const modelFn = `${convertFullyQualifiedNameToRelativePath(subKey)}Model.ts`;
                    const modelSource = sources.find(({ fileName }) => fileName === modelFn)!;
                    const fixedModelSource = new ModelFixProcessor(modelSource, discriminatorPropertyName).process();
                    sources.splice(sources.indexOf(modelSource), 1, fixedModelSource);
                  }
                });
              }
            });
          }
        });

        // remove the union type model file
        const unionFn = `${convertFullyQualifiedNameToRelativePath(baseKey)}Model.ts`;
        const unionSource = sources.find(({ fileName }) => fileName === unionFn)!;
        sources.splice(sources.indexOf(unionSource), 1);
      }
    });
  }
}
