const { parseArgs } = require('node:util');
const { basename } = require('node:path');
const { readFileSync } = require('node:fs');
const { readFile } = require('node:fs/promises');
const karmaChromeLauncher = require('karma-chrome-launcher');
const karmaCoverage = require('karma-coverage');
const karmaMocha = require('karma-mocha');
const karmaSpecReporter = require('karma-spec-reporter');
const karmaVite = require('karma-vite');
const karmaViewport = require('karma-viewport');
const MagicString = require('magic-string');
const postcss = require('postcss');
const cssnanoPlugin = require('cssnano');
const { karmaMochaConfig } = require('./.mocharc.cjs');
const reactPlugin = require('@vitejs/plugin-react');
const { pathToFileURL, fileURLToPath} = require("node:url");

// The current package, one of the packages in the `packages` dir
const cwd = pathToFileURL(`${process.cwd()}/`);
const root = pathToFileURL(`${__dirname}/`)

function loadMockConfig() {
  try {
    const content = readFileSync(new URL('test/mocks/config.json', cwd), 'utf8');
    return JSON.parse(content);
  } catch {
    console.log(`No mock files found for ${basename(fileURLToPath(cwd))}. Skipping...`);
    return {};
  }
}

// This plugin adds "__REGISTER__()" function definition everywhere where it finds
// the call for that function. It is necessary for a correct code for tests.
function loadRegisterJs() {
  return {
    enforce: 'pre',
    name: 'vite-hilla-register',
    async transform(code) {
      if (code.includes('__REGISTER__()') && !code.includes('function __REGISTER__')) {
        const registerCode = await readFile(new URL('scripts/register.js', root), 'utf8').then((c) =>
          c.replace('export', ''),
        );

        const _code = new MagicString(code);
        _code.prepend(registerCode);

        return {
          code: _code.toString(),
          map: _code.generateMap(),
        };
      }
    },
  };
}

const cssTransformer = postcss([cssnanoPlugin()]);

// This plugin transforms CSS to Constructible CSSStyleSheet for easy
// installation it to the document styles.
function constructCss() {
  let css = new Map();

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
    },
    async transform(_, id) {
      if (id.endsWith('.obj.css')) {
        const { content } = await cssTransformer.process(css.get(id));

        return {
          code: `const css = new CSSStyleSheet();css.replaceSync(${JSON.stringify(content)});export default css;`,
        };
      }
    },
  };
}

const {
  values: { coverage, watch: _watch },
} = parseArgs({
  options: {
    watch: {
      type: 'boolean',
      short: 'w',
    },
    coverage: {
      type: 'boolean',
    },
  },
  strict: false,
});

const isCI = !!process.env.CI;
const watch = !!_watch && !isCI;

module.exports = (config) => {
  const mocks = loadMockConfig();
  const tsconfig = JSON.parse(readFileSync(new URL('tsconfig.json', cwd), 'utf8'));
  const packageJson = JSON.parse(readFileSync(new URL('package.json', cwd), 'utf8'));

  config.set({
    basePath: fileURLToPath(cwd),

    plugins: [karmaVite, karmaMocha, karmaChromeLauncher, karmaCoverage, karmaSpecReporter, karmaViewport],
    middleware: ['vite'],

    browserNoActivityTimeout: isCI ? 30000 : 0,

    browsers: ['ChromeHeadless'],

    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-setuid-sandbox'],
      },
    },

    frameworks: ['mocha', 'vite', 'viewport'],

    files: [
      {
        pattern: 'test/**/*.+(test|spec).+(ts|js|tsx|jsx)',
        type: 'module',
        watched: false,
        served: false,
      },
    ],

    reporters: ['spec', !!coverage && 'coverage'].filter(Boolean),

    autoWatch: watch,
    singleRun: !watch,

    coverageReporter: {
      dir: '.coverage/',
      reporters: [!isCI && { type: 'html', subdir: 'html' }, { type: 'lcovonly', subdir: '.' }].filter(Boolean),
    },

    vite: {
      autoInit: false,
      config: {
        build: {
          target: 'esnext',
        },
        cacheDir: '.vite',
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
        plugins: [
          loadRegisterJs(),
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
      },
    },

    client: {
      mocha: karmaMochaConfig,
    },
    customContextFile: fileURLToPath(new URL('karma-context.html', root)),
    customDebugFile: fileURLToPath(new URL('karma-debug.html', root)),
    // Viewport configuration
    viewport: {
      breakpoints: [
        {
          name: 'mobile-portrait-320-480',
          size: {
            width: 320,
            height: 480,
          },
        },
        {
          name: 'screen-1024-768',
          size: {
            width: 1024,
            height: 768,
          },
        },
        {
          name: 'screen-1440-900',
          size: {
            width: 1440,
            height: 900,
          },
        },
        {
          name: 'screen-1980-1024',
          size: {
            width: 1980,
            height: 1024,
          },
        },
      ],
    },
  });
};
