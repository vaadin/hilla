import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type { ReferenceSchema } from '@hilla/generator-typescript-core/Schema.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import { convertFullyQualifiedNameToRelativePath } from '@hilla/generator-typescript-core/utils.js';
import { ModelFixProcessor } from './ModelFixProcessor.js';
import { SubTypesProcessor } from './SubTypesProcessor.js';
import { TypeFixProcessor } from './TypeFixProcessor.js';

export default class SubTypesPlugin extends Plugin {
  declare ['constructor']: typeof SubTypesPlugin;

  override get path(): string {
    return import.meta.url;
  }

  override async execute(storage: SharedStorage): Promise<void> {
    const { api, sources } = storage;

    const components = api.components?.schemas;

    if (!components) {
      return;
    }

    Object.entries(components).forEach(([baseKey, baseComponent]) => {
      if ('oneOf' in baseComponent && Array.isArray(baseComponent.oneOf)) {
        const fn = `${convertFullyQualifiedNameToRelativePath(baseKey)}.ts`;
        const source = sources.find(({ fileName }) => fileName === fn)!;
        const newSource = new SubTypesProcessor(baseKey, source, baseComponent.oneOf).process();
        sources.splice(sources.indexOf(source), 1, newSource);

        baseComponent.oneOf.forEach((schema) => {
          if ('$ref' in schema) {
            const path = (schema as ReferenceSchema).$ref;
            Object.entries(components).forEach(([subKey, subComponent]) => {
              if ('anyOf' in subComponent && subKey === path.substring('#/components/schemas/'.length)) {
                subComponent.anyOf?.forEach((s) => {
                  if ('properties' in s && '@type' in s.properties! && 'example' in s.properties['@type']) {
                    const typeValue = s.properties['@type'].example as string;
                    const subFn = `${convertFullyQualifiedNameToRelativePath(subKey)}.ts`;
                    const subSource = sources.find(({ fileName }) => fileName === subFn)!;
                    const fixedSource = new TypeFixProcessor(baseKey, subSource, typeValue).process();
                    sources.splice(sources.indexOf(subSource), 1, fixedSource);

                    const modelFn = `${convertFullyQualifiedNameToRelativePath(subKey)}Model.ts`;
                    const modelSource = sources.find(({ fileName }) => fileName === modelFn)!;
                    const fixedModelSource = new ModelFixProcessor(modelSource).process();
                    sources.splice(sources.indexOf(modelSource), 1, fixedModelSource);
                  }
                });
              }
            });
          }
        });

        const unionFn = `${convertFullyQualifiedNameToRelativePath(baseKey)}Union.ts`;
        const unionSource = sources.find(({ fileName }) => fileName === unionFn)!;
        sources.splice(sources.indexOf(unionSource), 1);
      }
    });
  }
}
