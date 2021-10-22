import SwaggerParser from '@apidevtools/swagger-parser';
import { readFile, writeFile } from 'fs/promises';
import type { OpenAPIV3 } from 'openapi-types';
import { resolve } from 'path';
import { createPrinter, NewLineKind } from 'typescript';
import type { Plugins } from './PluginManager';
import PluginManager from './PluginManager';
import type SharedStorage from './SharedStorage';

export type GeneratorConfig = Readonly<{
  outputDir: string;
  plugins: Plugins;
}>;

export default class Generator {
  #config: GeneratorConfig;

  static async #parseOpenAPI(input: string): Promise<OpenAPIV3.Document> {
    const rawOpenAPI = input.startsWith('{') ? input : await readFile(resolve(process.cwd(), input), 'utf8');
    return SwaggerParser.dereference(rawOpenAPI) as Promise<OpenAPIV3.Document>;
  }

  public constructor(config: GeneratorConfig) {
    this.#config = config;
  }

  public async process(input: string): Promise<void> {
    const [openAPI, manager] = await Promise.all([
      (this.constructor as typeof Generator).#parseOpenAPI(input),
      PluginManager.init(this.#config.plugins),
    ]);

    const storage: SharedStorage = {
      files: new Set(),
      openAPI,
      pluginStorage: new Map(),
    };

    manager.execute(storage);

    await this.#write(storage);
  }

  async #write(storage: SharedStorage): Promise<void> {
    const { outputDir } = this.#config;
    const printer = createPrinter({ newLine: NewLineKind.LineFeed });

    await Promise.all(
      Array.from(storage.files, async ({ path, source }) => {
        const content = printer.printFile(source);
        await writeFile(resolve(outputDir, path), content, 'utf8');
      })
    );
  }
}
