import Generator from '@vaadin/generator-typescript/Generator.js';
import type { PluginConstructor } from '@vaadin/generator-typescript/Plugin.js';
import { readFile } from 'fs/promises';
import Pino from 'pino';
import { URL } from 'url';

export const pathBase = './com/vaadin/fusion/parser/plugins/backbone';

export function createGenerator(plugins: readonly PluginConstructor[]): Generator {
  return new Generator(
    plugins,
    Pino({
      name: 'tsgen-test',
      level: 'debug',
    }),
  );
}

export async function loadInput(name: string, importMeta: string): Promise<string> {
  return readFile(new URL(`./${name}.json`, importMeta), 'utf8');
}
