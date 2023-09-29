import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import BackbonePlugin, { BackbonePluginSourceType } from '@hilla/generator-typescript-plugin-backbone';
import PluginError from '@hilla/generator-typescript-utils/PluginError.js';
import type { SourceFile } from 'typescript';
import BarrelProcessor from './BarrelProcessor.js';

export default class BarrelPlugin extends Plugin {
  override get path(): string {
    return import.meta.url;
  }

  // eslint-disable-next-line @typescript-eslint/require-await
  override async execute({ pluginStorage, sources }: SharedStorage): Promise<void> {
    const tags = pluginStorage.get(BackbonePlugin.BACKBONE_PLUGIN_FILE_TAGS) as WeakMap<SourceFile, string> | undefined;

    if (!tags) {
      throw new PluginError(`${BackbonePlugin.name} should be run first.`, this.constructor.name);
    }

    const endpoints = sources.filter((file) => tags.get(file) === BackbonePluginSourceType.Endpoint);
    const barrelFile = new BarrelProcessor(endpoints, this).process();
    sources.push(barrelFile);
  }
}
