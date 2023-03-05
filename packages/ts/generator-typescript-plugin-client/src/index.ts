import { constants } from 'node:fs';
import { access } from 'node:fs/promises';
import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import ClientProcessor from './ClientProcessor.js';

async function checkForCustomClientFile(directoryPath?: string): Promise<boolean> {
  try {
    // eslint-disable-next-line @typescript-eslint/no-use-before-define
    await access(new URL(`./${ClientPlugin.CUSTOM_CLIENT_FILE_NAME}.ts`, directoryPath), constants.F_OK);
    return true;
  } catch (e) {
    return false;
  }
}

export default class ClientPlugin extends Plugin {
  static readonly CLIENT_FILE_NAME = 'connect-client.default';
  static readonly CUSTOM_CLIENT_FILE_NAME = '../connect-client';

  static async getClientFileName(path?: string): Promise<string> {
    return (await checkForCustomClientFile(path))
      ? ClientPlugin.CUSTOM_CLIENT_FILE_NAME
      : ClientPlugin.CLIENT_FILE_NAME;
  }

  declare ['constructor']: typeof ClientPlugin;

  // eslint-disable-next-line class-methods-use-this
  override get path(): string {
    return import.meta.url;
  }

  override async execute({ sources, outputDir }: SharedStorage): Promise<void> {
    // the client file is created only if a custom client file is not found
    if (!(outputDir && (await checkForCustomClientFile(outputDir)))) {
      const clientFile = new ClientProcessor(this.constructor.CLIENT_FILE_NAME, this).process();
      sources.push(clientFile);
    }
  }
}
