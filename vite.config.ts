import { readFile } from 'node:fs/promises';
import { resolve } from 'node:path';
// eslint-disable-next-line import/no-extraneous-dependencies
import { defineConfig } from 'vite';

// The current package, one of the packages in the `packages` dir
const cwd = process.cwd();

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
  const [tsconfig, alias] = await Promise.all([
    readFile(resolve(cwd, 'tsconfig.json'), 'utf8').then((f) => JSON.parse(f)),
    prepareAliases(),
  ]);

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
    resolve: {
      alias,
    },
  };
});
