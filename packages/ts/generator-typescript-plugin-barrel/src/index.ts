import Plugin from '@vaadin/generator-typescript-core/Plugin.js';
import type SharedStorage from '@vaadin/generator-typescript-core/SharedStorage.js';
import BackbonePlugin, { BackbonePluginSourceType } from '@vaadin/generator-typescript-plugin-backbone';
import PluginError from '@vaadin/generator-typescript-utils/PluginError.js';
import type { SourceFile } from 'typescript';
import BarrelProcessor from './BarrelProcessor.js';

export default class BarrelPlugin extends Plugin {
  public override get path(): string {
    return import.meta.url;
  }

  public override async execute({ pluginStorage, sources }: SharedStorage): Promise<void> {
    const tags = pluginStorage.get(BackbonePlugin.BACKBONE_PLUGIN_FILE_TAGS) as WeakMap<SourceFile, string> | undefined;

    if (!tags) {
      throw new PluginError(`${BackbonePlugin.name} should be run first.`, this.constructor.name);
    }

    const endpoints = sources.filter((file) => tags.get(file) === BackbonePluginSourceType.Endpoint);
    const barrelFile = new BarrelProcessor(endpoints, this.logger).process();
    sources.push(barrelFile);
  }
}
