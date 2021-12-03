import Plugin from '@vaadin/generator-typescript-core/Plugin.js';
import type SharedStorage from '@vaadin/generator-typescript-core/SharedStorage.js';
import ClientProcessor from './ClientProcessor.js';

export default class ClientPlugin extends Plugin {
  public static readonly CLIENT_FILE_NAME = 'connect-client.default';

  public declare ['constructor']: typeof ClientPlugin;

  public override get path(): string {
    return import.meta.url;
  }

  public override async execute({ sources }: SharedStorage): Promise<void> {
    const clientFile = new ClientProcessor(this.constructor.CLIENT_FILE_NAME, this.logger).process();
    sources.push(clientFile);
  }
}
