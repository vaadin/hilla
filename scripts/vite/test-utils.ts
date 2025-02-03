/* eslint-disable no-console */
import { readFile } from 'node:fs/promises';
import { basename } from 'node:path';
import { fileURLToPath } from 'node:url';
import type { ViteUserConfig } from 'vitest/config.js';

export type MockConfig = Readonly<Record<string, string>>;
export type MockConfigLoaderOptions = Readonly<{
  cwd: URL;
}>;

export async function loadMockConfig({ cwd }: MockConfigLoaderOptions): Promise<MockConfig> {
  try {
    const content = await readFile(new URL('test/mocks/config.json', cwd), 'utf8');
    return JSON.parse(content);
  } catch {
    console.log(`No mock files found for ${basename(fileURLToPath(cwd))}. Skipping...`);
    return {};
  }
}

export function createResolver(mocks: Readonly<Record<string, string>>, cwd: URL): ViteUserConfig {
  return {
    resolve: {
      alias: Object.entries(mocks).map(([find, file]) => {
        const replacement = fileURLToPath(new URL(`test/mocks/${file}`, cwd));

        return {
          customResolver(_, importer) {
            if (importer?.includes('/mocks/')) {
              return false;
            }

            return replacement;
          },
          find,
          replacement,
        };
      }),
    },
  };
}
