import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type { ReferenceSchema } from '@hilla/generator-typescript-core/Schema.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import { convertFullyQualifiedNameToRelativePath } from '@hilla/generator-typescript-core/utils.js';
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

    Object.entries(components).forEach(([key, component]) => {
      if ('oneOf' in component && Array.isArray(component.oneOf)) {
        const fn = `${convertFullyQualifiedNameToRelativePath(key)}.ts`;
        const source = sources.find(({ fileName }) => fileName === fn)!;
        const newSource = new SubTypesProcessor(key, source, component.oneOf).process();
        sources.splice(sources.indexOf(source), 1, newSource);

        component.oneOf.forEach((schema) => {
          if ('$ref' in schema) {
            const path = (schema as ReferenceSchema).$ref;
            Object.entries(components).forEach(([key2, component2]) => {
              if ('anyOf' in component2 && key2 === path.substring('#/components/schemas/'.length)) {
                component2.anyOf?.forEach((s) => {
                  if ('properties' in s && '@type' in s.properties! && 'example' in s.properties['@type']) {
                    const typeValue = s.properties['@type'].example as string;
                    const fn2 = `${convertFullyQualifiedNameToRelativePath(key2)}.ts`;
                    const source2 = sources.find(({ fileName }) => fileName === fn2)!;
                    const fixedSource = new TypeFixProcessor(source2, typeValue).process();
                    sources.splice(sources.indexOf(source2), 1, fixedSource);
                  }
                });
              }
            });
          }
        });
      }
    });
  }
}
