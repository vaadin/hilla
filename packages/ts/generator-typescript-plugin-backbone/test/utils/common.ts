import Generator from '@vaadin/generator-typescript-core/Generator.js';
import type { PluginConstructor } from '@vaadin/generator-typescript-core/Plugin.js';
import createLogger from '@vaadin/generator-typescript-utils/createLogger.js';
import { readFile } from 'fs/promises';
import { URL } from 'url';

export const pathBase = 'com/vaadin/fusion/parser/plugins/backbone';

export function createGenerator(plugins: readonly PluginConstructor[]): Generator {
  return new Generator(plugins, createLogger({ name: 'tsgen-test', verbose: true }));
}

export async function loadInput(name: string, importMeta: string): Promise<string> {
  return readFile(new URL(`./${name}.json`, importMeta), 'utf8');
}
