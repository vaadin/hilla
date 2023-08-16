import { readFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { PackageJson, TsConfigJson } from 'type-fest';
// eslint-disable-next-line import/no-extraneous-dependencies
import { defineConfig, type UserConfig } from 'vite';

// The current package, one of the packages in the `packages` dir
const cwd = pathToFileURL(`${process.cwd()}/`);

async function loadMockConfig() {
  try {
    const content = await readFile(new URL('test/mocks/config.json', cwd), 'utf8');
    return JSON.parse(content) as Record<string, string>;
  } catch (e: unknown) {
    console.error(e);
    return {};
  }
}

// https://vitejs.dev/config/
export default defineConfig(async () => {
  const [tsconfig, mocks, packageJson] = await Promise.all([
    readFile(new URL('tsconfig.json', cwd), 'utf8').then((f) => JSON.parse(f) as TsConfigJson),
    loadMockConfig(),
    readFile(new URL('package.json', cwd), 'utf8').then((f) => JSON.parse(f) as PackageJson),
  ]);

  return {
    build: {
      target: 'esnext',
    },
    define: {
      __VERSION__: `'${packageJson.version ?? '0.0.0'}'`,
    },
    esbuild: {
      tsconfigRaw: {
        ...tsconfig,
        compilerOptions: {
          ...(tsconfig.compilerOptions as any),
          useDefineForClassFields: false,
        },
      },
    },
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
  } satisfies UserConfig;
});
