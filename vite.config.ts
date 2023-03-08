import { readFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import { defineConfig } from 'vite';

const cwd = process.cwd();

const aliasConfig = cwd.includes('hilla-frontend')
  ? {
      resolve: {
        alias: {
          'a-atmosphere-javascript': resolve(cwd, 'test/mocks/atmosphere.ts'),
        },
      },
    }
  : {};

// https://vitejs.dev/config/
export default defineConfig(async () => {
  const tsconfig = JSON.parse(await readFile(resolve(cwd, '../../../tsconfig.json'), 'utf8'));

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
    ...aliasConfig,
  };
});
