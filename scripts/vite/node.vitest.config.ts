import { readFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { PackageJson } from 'type-fest';
import { defineConfig } from 'vitest/config';
import { loadRegisterJs } from './plugins.js';

export const isCI = process.env.CI === 'true';

export const root = new URL('../../', import.meta.url);
export const cwd = pathToFileURL(`${process.cwd()}/`);

export const packageJson = await readFile(new URL('./package.json', cwd), 'utf8').then(
  (content) => JSON.parse(content) as PackageJson,
);

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
  plugins: [loadRegisterJs({ root })],
  test: {
    coverage: {
      enabled: false,
      all: true,
      provider: 'v8',
      reportsDirectory: fileURLToPath(new URL('.coverage/', cwd)),
      clean: true,
      reporter: isCI ? ['lcov'] : ['html'],
    },
    includeTaskLocation: !isCI,
  },
});
