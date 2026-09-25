import { readFile } from 'node:fs/promises';
import { join } from 'node:path';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import type { PluginConstructor } from '@vaadin/hilla-generator-core/Plugin.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import { typeCheck as typeCheckIn } from '@vaadin/hilla-generator-utils/testing/typeCheck.js';

export const pathBase = 'com/vaadin/hilla/parser/plugins/subtypes';

export function createGenerator(plugins: readonly PluginConstructor[]): Generator {
  return new Generator(plugins, { logger: new LoggerFactory({ name: 'tsgen-test-subtypes', verbose: true }) });
}

export async function loadInput(name: string, importMeta: string): Promise<string> {
  return readFile(new URL(`./${name}.json`, importMeta), 'utf8');
}

/**
 * Type-checks the generated sources in a directory of this package, so that the
 * imported Hilla packages resolve.
 */
export async function typeCheck(
  files: readonly File[],
  extraSources: Readonly<Record<string, string>> = {},
): Promise<readonly string[]> {
  return typeCheckIn(join(import.meta.dirname, 'generated'), files, extraSources);
}
