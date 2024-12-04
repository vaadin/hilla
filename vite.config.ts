import { readFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
import reactPlugin from '@vitejs/plugin-react';
import type { PackageJson } from 'type-fest';
import { defineConfig } from 'vite';
import { constructCss, loadRegisterJs } from './scripts/vite/plugins.js';
import { loadMockConfig } from './scripts/vite/test-utils.js';

const root = new URL('./', import.meta.url);
const cwd = pathToFileURL(`${process.cwd()}/`);

const [packageJson, mocks] = await Promise.all([
  readFile(new URL('./package.json', cwd), 'utf8').then((content) => JSON.parse(content) as PackageJson),
  loadMockConfig({ cwd }),
]);

export default defineConfig({
  build: {
    target: 'esnext',
  },
  cacheDir: '.vite',
  esbuild: {
    define: {
      __NAME__: JSON.stringify(packageJson.name ?? '@hilla/unknown'),
      __VERSION__: JSON.stringify(packageJson.version ?? '0.0.0'),
    },
    supported: {
      decorators: false,
      'top-level-await': true,
    },
  },
  plugins: [
    loadRegisterJs({ root }),
    constructCss(),
    reactPlugin({
      include: '**/*.tsx',
      babel: {
        plugins: [
          [
            'module:@preact/signals-react-transform',
            {
              mode: 'all',
            },
          ],
        ],
      },
    }),
  ],
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
});
