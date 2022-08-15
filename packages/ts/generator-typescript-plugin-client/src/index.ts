import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import { existsSync } from 'fs';
import { fileURLToPath } from 'url';
import ClientProcessor from './ClientProcessor.js';

export default class ClientPlugin extends Plugin {
  public static readonly CLIENT_FILE_NAME = 'connect-client.default';
  public static readonly CUSTOM_CLIENT_FILE_NAME = '../connect-client';

  public declare ['constructor']: typeof ClientPlugin;

  public override get path(): string {
    return import.meta.url;
  }

  public override async execute({ sources, outputDir }: SharedStorage): Promise<void> {
    if (!ClientPlugin.clientFile(outputDir).custom) {
      const clientFile = new ClientProcessor(this.constructor.CLIENT_FILE_NAME, this).process();
      sources.push(clientFile);
    }
  }

  public static clientFile(path: string): { name: string; custom: boolean } {
    return path && existsSync(fileURLToPath(`${path}/${ClientPlugin.CUSTOM_CLIENT_FILE_NAME}.ts`))
      ? { name: ClientPlugin.CUSTOM_CLIENT_FILE_NAME, custom: true }
      : { name: ClientPlugin.CLIENT_FILE_NAME, custom: false };
  }
}
