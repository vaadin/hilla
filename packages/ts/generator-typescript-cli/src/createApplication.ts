import SwaggerParser from '@apidevtools/swagger-parser';
import Generator from '@vaadin/generator-typescript/Generator';
import PluginManager from '@vaadin/generator-typescript/PluginManager';
import ReferenceResolver from '@vaadin/generator-typescript/ReferenceResolver';
import { readFile, writeFile } from 'fs/promises';
import { resolve } from 'path';
import Pino from 'pino';
import { URL } from 'url';

type AppOptions = Readonly<{
  outputDir: string;
  plugins: readonly string[];
  verbose?: boolean;
}>;

const cwd = process.cwd();

function read(path: string): Promise<string> {
  return readFile(resolve(cwd, path), 'utf8');
}

async function createPluginManager(
  plugins: readonly string[],
  resolver: ReferenceResolver,
  logger: Pino.Logger,
): Promise<PluginManager> {
  const manager = new PluginManager(resolver, logger);

  const resolvedPluginPaths: readonly URL[] = Array.from(new Set(plugins), (plugin) => new URL(plugin, cwd));

  logger.info({ paths: resolvedPluginPaths }, 'User-defined plugins');

  (await Promise.all(resolvedPluginPaths.map(async (path) => manager.load(path)))).forEach((PluginClass) =>
    manager.add(PluginClass),
  );

  return manager;
}

export default async function createApplication(input: string, { outputDir, plugins, verbose }: AppOptions) {
  const logger = Pino({
    name: 'tsgen',
    level: verbose ? 'debug' : 'info',
  });

  const parser = new SwaggerParser();
  const resolver = new ReferenceResolver(parser);
  const manager = await createPluginManager(plugins, resolver, logger);

  const generator = new Generator(parser, manager, logger);

  const rawInput = input.startsWith('{') ? input : await read(input);

  const files = await generator.process(rawInput);

  await Promise.all(
    files.map(async (file) => writeFile(resolve(cwd, outputDir, file.name), new Uint8Array(await file.arrayBuffer()))),
  );
}
