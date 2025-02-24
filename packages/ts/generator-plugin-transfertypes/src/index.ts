import Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import type { SharedStorage } from '@vaadin/hilla-generator-core/SharedStorage.js';
import type { Writable } from 'type-fest';
import ts from 'typescript';

export default class TransferTypesPlugin extends Plugin {
  override get path(): string {
    return import.meta.url;
  }

  override execute({ api: { components }, transferTypes }: SharedStorage): void {
    transferTypes.set('com.vaadin.hilla.runtime.transfertypes.File', () => ts.factory.createTypeReferenceNode('File'));

    if (components?.schemas) {
      (components as Writable<typeof components>).schemas = Object.fromEntries(
        Object.entries(components.schemas).filter(([key]) => key !== 'com.vaadin.hilla.runtime.transfertypes.File'),
      );
    }
  }
}
