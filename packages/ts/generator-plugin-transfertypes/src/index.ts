import Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import type { SharedStorage, TransferTypeMaker } from '@vaadin/hilla-generator-core/SharedStorage.js';
import type { OpenAPIV3 } from 'openapi-types';
import type { Writable } from 'type-fest';
import { factory, type Identifier } from 'typescript';

type ReplacedTypeMaker = (schema?: OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject) => TransferTypeMaker;

type FromModule = Readonly<{
  module: string;
  named?: string;
  default?: string;
}>;

type ReplacerKey = 'x-from-module' | 'x-model-from-module';

function createReplacedTypeMaker(name: string, key: ReplacerKey): ReplacedTypeMaker {
  return (schema) =>
    ({ dependencies: { imports }, typeArguments }) => {
      let id: Identifier | undefined;

      if (schema && key in schema) {
        const fromModule = (schema as Record<string, unknown>)[key] as FromModule;

        if (fromModule.named) {
          id =
            imports.named.getIdentifier(fromModule.module, fromModule.named) ??
            imports.named.add(fromModule.module, fromModule.named);
        }

        if (fromModule.default) {
          id =
            imports.default.getIdentifier(fromModule.module) ??
            imports.default.add(fromModule.module, fromModule.default);
        }
      }

      return factory.createTypeReferenceNode(id ?? name, typeArguments);
    };
}

type ReplacedTypes = Readonly<Record<string, ReplacedTypeMaker>>;

const replacedTypes: ReplacedTypes = Object.fromEntries([
  ...['File', 'Signal', 'NumberSignal', 'ValueSignal', 'ListSignal'].map((name) => [
    `com.vaadin.hilla.runtime.transfertypes.${name}`,
    createReplacedTypeMaker(name, 'x-from-module'),
  ]),
  ...['Order', 'Page', 'Pageable', 'Slice', 'Sort'].map((name) => [
    `com.vaadin.hilla.mappedtypes.${name}`,
    createReplacedTypeMaker(name, 'x-from-module'),
  ]),
  ...['Order', 'Page', 'Pageable', 'Slice', 'Sort'].map((name) => [
    `com.vaadin.hilla.mappedtypes.${name}Model`,
    createReplacedTypeMaker(`${name}`, 'x-model-from-module'),
  ]),
]);

export default class TransferTypesPlugin extends Plugin {
  declare ['constructor']: typeof TransferTypesPlugin;

  override get path(): string {
    return import.meta.url;
  }

  override execute({ api: { components }, transferTypes }: SharedStorage): void {
    for (const [key, value] of Object.entries(replacedTypes)) {
      // For model transfer types, look up the schema under the base name (without 'Model' suffix)
      const schemaKey = key.endsWith('Model') ? key.slice(0, -5) : key;
      transferTypes.set(key, value(components?.schemas?.[schemaKey]));
    }

    if (components?.schemas) {
      const keys = Object.keys(replacedTypes);

      (components as Writable<typeof components>).schemas = Object.fromEntries(
        Object.entries(components.schemas).filter(([key]) => !keys.includes(key)),
      );
    }
  }
}
