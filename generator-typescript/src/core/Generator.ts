import SwaggerParser from '@apidevtools/swagger-parser';
import { readFile } from 'fs/promises';
import type { OpenAPIV3 } from 'openapi-types';
import { resolve } from 'path';
import Pino from 'pino';
import { createPrinter, NewLineKind } from 'typescript';
import { defaultOutputDir } from './config.default';
import type { PluginsConfiguration } from './PluginManager';
import PluginManager from './PluginManager';
import ReferenceResolver from './ReferenceResolver';
import type SharedStorage from './SharedStorage';

export type GeneratorConfig = Readonly<{
  outputDir?: string;
  plugins?: PluginsConfiguration;
}>;

export type GeneratorOptions = GeneratorConfig &
  Readonly<{
    verbose?: boolean;
  }>;

const cwd = process.cwd();

export default class Generator {
  public static async init(configPath: string, options: GeneratorOptions): Promise<Generator> {
    const logger = Pino({
      name: 'tsgen',
      level: options.verbose ? 'debug' : 'info',
    });

    const configAbsolutePath = resolve(cwd, configPath);

    logger.info(`Loading config by path: '${configAbsolutePath}'`);

    const config: GeneratorConfig = JSON.parse(await readFile(configAbsolutePath, 'utf8'));

    return new Generator(config, logger);
  }

  readonly #config: GeneratorConfig;
  readonly #logger: Pino.Logger;
  readonly #parser: SwaggerParser;

  private constructor(config: GeneratorConfig, logger: Pino.Logger) {
    this.#config = config;
    this.#logger = logger;
    this.#parser = new SwaggerParser();
  }

  public async process(input: string): Promise<void> {
    const [api, manager] = await Promise.all([
      this.#parseOpenAPI(this.#parser, input),
      PluginManager.init(this.#config.plugins, new ReferenceResolver(this.#parser), this.#logger),
    ]);

    const storage: SharedStorage = {
      api,
      apiRefs: this.#parser.$refs,
      sources: new Map(),
      pluginStorage: new Map(),
    };

    await manager.execute(storage);
    await this.#write(storage);
  }

  async #parseOpenAPI(parser: SwaggerParser, input: string): Promise<OpenAPIV3.Document> {
    const rawOpenAPI = input.startsWith('{') ? input : await readFile(resolve(cwd, input), 'utf8');
    return parser.bundle(rawOpenAPI) as Promise<OpenAPIV3.Document>;
  }

  async #write(storage: SharedStorage): Promise<void> {
    const { outputDir = defaultOutputDir } = this.#config;
    const printer = createPrinter({ newLine: NewLineKind.LineFeed });

    // await Promise.all(
    //   Array.from(storage.files.entries(), async ([path, source]) => {
    //     const content = printer.printFile(createSourceFile(source);
    //     await writeFile(resolve(outputDir, path), content, 'utf8');
    //   })
    // );
  }
}
