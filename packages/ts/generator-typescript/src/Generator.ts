import SwaggerParser from '@apidevtools/swagger-parser';
import type { OpenAPIV3 } from 'openapi-types';
import Pino from 'pino';
import type { ReadonlyDeep } from 'type-fest';
import ts from 'typescript';
import ReferenceResolver from '../ReferenceResolver.js';
import type GeneratorIO from './GeneratorIO.js';
import PluginManager from './PluginManager.js';
import type SharedStorage from './SharedStorage.js';
import File from './File.js';

const cwd = process.cwd();

export type GeneratorOptions = Readonly<{
  io: GeneratorIO;
  outputDir: string;
  plugins: readonly string[];
  verbose?: boolean;
}>;

export default class Generator {
  public static async create({ io, outputDir, plugins, verbose }: GeneratorOptions): Generator {
    const logger = Pino({
      name: 'tsgen',
      level: verbose ? 'debug' : 'info',
    });

    const parser = new SwaggerParser();
    const resolver = new ReferenceResolver(parser);
    const manager = await PluginManager.create(plugins, resolver, logger);

    return new Generator(parser, manager, io, logger);
  }

  readonly #io: GeneratorIO;
  readonly #logger: Pino.Logger;
  readonly #manager: PluginManager;
  readonly #parser: SwaggerParser;

  private constructor(parser: SwaggerParser, manager: PluginManager, io: GeneratorIO, logger: Pino.Logger) {
    this.#io = io;
    this.#logger = logger;
    this.#manager = manager;
    this.#parser = parser;
  }

  public async process(input: string): Promise<readonly File[]> {
    const processedInput = input.startsWith('{') ? input : await this.#io.read(input);
    const api = (await this.#parser.bundle(JSON.parse(processedInput))) as ReadonlyDeep<OpenAPIV3.Document>;

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
