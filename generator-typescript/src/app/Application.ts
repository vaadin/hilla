import SwaggerParser from '@apidevtools/swagger-parser';
import { readFile, writeFile } from 'fs/promises';
import { resolve } from 'path';
import Pino from 'pino';
import Generator from '../core/Generator.js';
import PluginManager from '../core/PluginManager.js';
import ReferenceResolver from '../core/ReferenceResolver.js';

export type Plugins = Readonly<{
  disable: readonly string[];
  use: readonly string[];
}>;

export type AppConfiguration = Readonly<{
  plugins?: Plugins;
  outputDir?: string;
}>;

export type AppOptions = AppConfiguration &
  Readonly<{
    verbose?: boolean;
  }>;

const defaultOutputDir = 'frontend/generated';
const cwd = process.cwd();

const builtInPluginPaths: readonly string[] = [resolve(import.meta.url, '../plugins/BackbonePlugin')];

function filterUniqueAndAbsolutize(paths?: readonly string[]): readonly string[] {
  return paths ? Array.from(new Set(paths), (path) => resolve(cwd, path)) : [];
}

function read(path: string): Promise<string> {
  return readFile(resolve(cwd, path), 'utf8');
}

export default class Application {
  readonly #options: AppOptions;

  public constructor(options: AppOptions) {
    this.#options = options;
  }

  public ['constructor']: typeof Application;

  public async execute(configPath: string, input: string): Promise<void> {
    const logger = Pino({
      name: 'tsgen',
      level: this.#options.verbose ? 'debug' : 'info',
    });

    const configAbsolutePath = resolve(cwd, configPath);

    logger.info(`Loading config by path: '${configAbsolutePath}'`);

    const { plugins, outputDir = defaultOutputDir }: AppConfiguration = Object.assign(
      JSON.parse(await read(configAbsolutePath)),
      this.#options,
    );

    const parser = new SwaggerParser();
    const resolver = new ReferenceResolver(parser);
    const manager = await this.#createPluginManager(plugins, resolver, logger);

    const generator = new Generator(parser, manager, logger);

    const rawInput = input.startsWith('{') ? input : await read(input);

    const files = await generator.process(rawInput);

    await Promise.all(
      files.map(async (file) =>
        writeFile(resolve(cwd, outputDir, file.name), new Uint8Array(await file.arrayBuffer())),
      ),
    );
  }

  async #createPluginManager(
    plugins: Plugins | undefined,
    resolver: ReferenceResolver,
    logger: Pino.Logger,
  ): Promise<PluginManager> {
    const manager = new PluginManager(resolver, logger);

    const disabledPluginsPaths = filterUniqueAndAbsolutize(plugins?.disable);
    const userDefinedPlugins = filterUniqueAndAbsolutize(plugins?.use);

    logger.info({ paths: disabledPluginsPaths }, 'Disabled built-in plugins');
    logger.info({ paths: userDefinedPlugins }, 'User-defined plugins');

    const builtInPlugins = builtInPluginPaths.filter((path) => !disabledPluginsPaths.includes(path));

    (await Promise.all([...builtInPlugins, ...userDefinedPlugins].map(async (path) => manager.load(path)))).forEach(
      (PluginClass) => manager.add(PluginClass),
    );

    return manager;
  }
}
