import { readdir, readFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
// eslint-disable-next-line import/no-extraneous-dependencies
import { defineConfig } from 'vite';

// The current package, one of the packages in the `packages` dir
const cwd = process.cwd();

// The monorepo root directory
const packagesDir = fileURLToPath(new URL('packages/ts', import.meta.url));

async function prepareAliases() {
  try {
    const contents = await readFile(resolve(cwd, 'test/mocks.json'), 'utf8');
    return Object.fromEntries(
      Object.entries(JSON.parse(contents) as Record<string, string>).map(([key, value]) => [key, resolve(cwd, value)]),
    );
  } catch {
    return {};
  }
}

// https://vitejs.dev/config/
export default defineConfig(async () => {
  const [packages, tsconfig, alias] = await Promise.all([
    readdir(packagesDir).then((p) => p.map((filename) => resolve(packagesDir, filename))),
    readFile(resolve(cwd, 'tsconfig.json'), 'utf8').then((f) => JSON.parse(f)),
    prepareAliases(),
  ]);

  const index = packages.indexOf(cwd);

  return {
    build: {
      target: 'esnext',
    },
    esbuild: {
      tsconfigRaw: {
        ...tsconfig,
        compilerOptions: {
          ...tsconfig.compilerOptions,
          useDefineForClassFields: false,
        },
      },
    },
    server: {
      // necessary to avoid simultaneous port occupation
      port: 5173 + index,
    },
    resolve: {
      alias,
    },
  };
});
