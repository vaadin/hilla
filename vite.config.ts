/* eslint-disable no-console */
import { readFile } from 'node:fs/promises';
import { basename } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import cssnanoPlugin from 'cssnano';
import type { TsconfigRaw } from 'esbuild';
import MagicString from 'magic-string';
import postcss from 'postcss';
import type { PackageJson } from 'type-fest';
import { type Alias, defineConfig, type Plugin } from 'vite';

const root = new URL('./', import.meta.url);

const cssTransformer = postcss([cssnanoPlugin()]);

// This plugin transforms CSS to Constructible CSSStyleSheet for easy
// installation it to the document styles.
function constructCss(): Plugin {
  const css = new Map();

  return {
    enforce: 'post',
    name: 'vite-construct-css',
    async load(id) {
      if (id.endsWith('.obj.css')) {
        const content = await readFile(id, 'utf8');
        css.set(id, content);
        return {
          code: '',
        };
      }

      return null;
    },
    async transform(_, id) {
      if (id.endsWith('.obj.css')) {
        const { content } = await cssTransformer.process(css.get(id));

        return {
          code: `const css = new CSSStyleSheet();css.replaceSync(${JSON.stringify(content)});export default css;`,
        };
      }

      return null;
    },
  };
}

// This plugin adds "__REGISTER__()" function definition everywhere where it finds
// the call for that function. It is necessary for a correct code for tests.
function loadRegisterJs(): Plugin {
  return {
    enforce: 'pre',
    name: 'vite-hilla-register',
    async transform(code) {
      if (code.includes('__REGISTER__()') && !code.includes('function __REGISTER__')) {
        const registerCode = await readFile(new URL('./scripts/register.js', root), 'utf8').then((c) =>
          c.replace('export', ''),
        );

        const _code = new MagicString(code);
        _code.prepend(registerCode);

        return {
          code: _code.toString(),
          map: _code.generateMap(),
        };
      }

      return null;
    },
  };
}

async function load<T>(path: URL): Promise<T>;
async function load<T>(path: URL, ignoreFailure: string): Promise<T | null>;
async function load<T>(path: URL, ignoreFailure?: string): Promise<T | null> {
  try {
    const content = await readFile(path, 'utf8');
    return JSON.parse(content);
  } catch (e: unknown) {
    if (ignoreFailure) {
      console.log(ignoreFailure);
      return null;
    }

    throw e;
  }
}

const {
  values: { group },
} = parseArgs({
  options: {
    group: {
      type: 'string',
    },
  },
  strict: false,
});

const cwd = new URL(`./packages/ts/${group}/`, root);

export default defineConfig(async () => {
  const [tsconfig, packageJson, mocks] = await Promise.all([
    load<TsconfigRaw>(new URL('./tsconfig.json', cwd)),
    load<PackageJson>(new URL('./package.json', cwd)),
    load<Record<string, string>>(
      new URL('./test/mocks/config.json', cwd),
      `No mock files found for ${basename(fileURLToPath(cwd))}. Skipping`,
    ),
  ]);

  return {
    build: {
      target: 'esnext',
    },
    cacheDir: fileURLToPath(new URL('.vite', cwd)),
    esbuild: {
      define: {
        __NAME__: `'${packageJson.name ?? '@hilla/unknown'}'`,
        __VERSION__: `'${packageJson.version ?? '0.0.0'}'`,
      },
      tsconfigRaw: {
        ...tsconfig,
        compilerOptions: {
          ...tsconfig.compilerOptions,
          useDefineForClassFields: false,
        },
      },
    },
    plugins: [constructCss(), loadRegisterJs()],
    resolve: {
      alias: Object.entries(mocks ?? {}).map(([find, file]) => {
        const replacement = fileURLToPath(new URL(`./test/mocks/${file}`, cwd));

        return {
          customResolver(_, importer) {
            if (importer?.includes('/mocks/')) {
              return false;
            }

            return replacement;
          },
          find,
          replacement,
        } satisfies Alias;
      }),
    },
  };
});
