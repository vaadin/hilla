import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import { open } from 'fs/promises';
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
    // the client file is created only if a custom client file is not found
    if (!(outputDir && (await ClientPlugin.checkForCustomClientFile(outputDir)))) {
      const clientFile = new ClientProcessor(this.constructor.CLIENT_FILE_NAME, this).process();
      sources.push(clientFile);
    }
  }

  private static async checkForCustomClientFile(path?: string): Promise<boolean> {
    const dir = path && path.startsWith('file:') ? fileURLToPath(path) : path;
    return !!(dir && (await open(`${dir}/${ClientPlugin.CUSTOM_CLIENT_FILE_NAME}.ts`, 'r')));
  }

  public static async getClientFileName(path?: string): Promise<string> {
    return (await ClientPlugin.checkForCustomClientFile(path))
      ? ClientPlugin.CUSTOM_CLIENT_FILE_NAME
      : ClientPlugin.CLIENT_FILE_NAME;
  }
}
