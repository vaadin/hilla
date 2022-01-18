import SwaggerParser from '@apidevtools/swagger-parser';
import type LoggerFactory from '@vaadin/generator-typescript-utils/LoggerFactory.js';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import ts from 'typescript';
import File from './File.js';
import type { PluginConstructor } from './Plugin.js';
import PluginManager from './PluginManager.js';
import ReferenceResolver from './ReferenceResolver.js';
import type SharedStorage from './SharedStorage.js';

export default class Generator {
  readonly #logger: LoggerFactory;
  readonly #manager: PluginManager;
  readonly #parser: SwaggerParser;

  public constructor(plugins: readonly PluginConstructor[], logger: LoggerFactory) {
    this.#parser = new SwaggerParser();
    this.#manager = new PluginManager(plugins, new ReferenceResolver(this.#parser), logger);
    this.#logger = logger;
  }

  public async process(input: string): Promise<readonly File[]> {
    this.#logger.global.debug('Processing OpenAPI');
    const api = (await this.#parser.bundle(JSON.parse(input))) as ReadonlyDeep<OpenAPIV3.Document>;

    const storage: SharedStorage = {
      api,
      apiRefs: this.#parser.$refs,
      pluginStorage: new Map(),
      sources: [],
    };

    this.#logger.global.debug('Executing plugins');
    await this.#manager.execute(storage);

    this.#logger.global.debug('Printing files');
    const printer = ts.createPrinter({ newLine: ts.NewLineKind.LineFeed });

    return storage.sources.map((file) => new File([printer.printFile(file)], file.fileName));
  }
}
