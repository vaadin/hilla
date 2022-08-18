import Generator from '@hilla/generator-typescript-core/Generator.js';
import type { PluginConstructor } from '@hilla/generator-typescript-core/Plugin.js';
import LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import { readFile } from 'fs/promises';
import { URL } from 'url';

export const pathBase = 'dev/hilla/parser/plugins/push';

export function createGenerator(plugins: readonly PluginConstructor[]): Generator {
  return new Generator(plugins, { logger: new LoggerFactory({ name: 'tsgen-test-push', verbose: true }) });
}

export async function loadInput(name: string, importMeta: string): Promise<string> {
  return readFile(new URL(`./${name}.json`, importMeta), 'utf8');
}
