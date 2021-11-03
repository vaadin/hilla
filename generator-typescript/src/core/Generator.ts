import type SwaggerParser from '@apidevtools/swagger-parser';
import type { OpenAPIV3 } from 'openapi-types';
import type Pino from 'pino';
import type { ReadonlyDeep } from 'type-fest';
import ts from 'typescript';
import type PluginManager from './PluginManager.js';
import type SharedStorage from './SharedStorage.js';
import File from './File.js';

export default class Generator {
  readonly #manager: PluginManager;
  readonly #logger: Pino.Logger;
  readonly #parser: SwaggerParser;

  public constructor(parser: SwaggerParser, manager: PluginManager, logger: Pino.Logger) {
    this.#logger = logger;
    this.#manager = manager;
    this.#parser = parser;
  }

  public async process(input: string): Promise<readonly File[]> {
    const api = (await this.#parser.bundle(JSON.parse(input))) as ReadonlyDeep<OpenAPIV3.Document>;

    const storage: SharedStorage = {
      api,
      apiRefs: this.#parser.$refs,
      pluginStorage: new Map(),
      sources: [],
    };

    await this.#manager.execute(storage);

    const printer = ts.createPrinter({ newLine: ts.NewLineKind.LineFeed });

    return storage.sources.map((file) => new File([printer.printFile(file)], file.fileName));
  }
}
