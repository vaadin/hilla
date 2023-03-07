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
export default defineConfig({
  build: {
    target: 'esnext',
  },
  ...aliasConfig,
});
