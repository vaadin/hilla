import Plugin from '@vaadin/generator-typescript-core/Plugin.js';
import type SharedStorage from '@vaadin/generator-typescript-core/SharedStorage.js';
import ClientProcessor from './ClientProcessor.js';

export default class ClientPlugin extends Plugin {
  public override get path(): string {
    return import.meta.url;
  }

  public override async execute({ sources }: SharedStorage): Promise<void> {
    const clientFile = new ClientProcessor(this.logger).process();
    sources.push(clientFile);
  }
}
