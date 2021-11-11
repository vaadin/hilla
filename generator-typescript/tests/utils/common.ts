import SwaggerParser from '@apidevtools/swagger-parser';
import { readFile } from 'fs/promises';
import Pino from 'pino';
import { fileURLToPath, URL } from 'url';
import Generator from '../../src/core/Generator.js';
import type { PluginConstructor } from '../../src/core/Plugin.js';
import PluginManager from '../../src/core/PluginManager.js';
import ReferenceResolver from '../../src/core/ReferenceResolver.js';

export const pathBase = './com/vaadin/fusion/parser/plugins/backbone';

export function createGenerator(plugins: readonly PluginConstructor[]): Generator {
  const logger = Pino({
    name: 'tsgen-test',
    level: 'debug',
  });
  const parser = new SwaggerParser();
  const resolver = new ReferenceResolver(parser);

  const manager = new PluginManager(resolver, logger);
  plugins.forEach((plugin) => manager.add(plugin));

  return new Generator(parser, manager, logger);
}

export async function loadInput(name: string, importMeta: string): Promise<string> {
  const jsonUrl = new URL(`./resources/${name}.json`, importMeta);

  return readFile(fileURLToPath(jsonUrl), 'utf8');
}
