const { parseArgs } = require('node:util');
const { basename, join } = require('node:path');
const { readFileSync } = require('node:fs');
const karmaChromeLauncher = require('karma-chrome-launcher');
const karmaCoverage = require('karma-coverage');
const karmaMocha = require('karma-mocha');
const karmaSpecReporter = require('karma-spec-reporter');
const karmaVite = require('karma-vite');

// The current package, one of the packages in the `packages` dir
const cwd = process.cwd();

function loadMockConfig() {
  try {
    const content = readFileSync(join(cwd, 'test/mocks/config.json'), 'utf8');
    return JSON.parse(content);
  } catch {
    console.log(`No mock files found for ${basename(cwd)}. Skipping...`);
    return {};
  }
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
  const tsconfig = JSON.parse(readFileSync(join(cwd, 'tsconfig.json'), 'utf8'));
  const packageJson = JSON.parse(readFileSync(join(cwd, 'package.json'), 'utf8'));

  config.set({
    basePath: cwd,

    plugins: [karmaVite, karmaMocha, karmaChromeLauncher, karmaCoverage, karmaSpecReporter],
    middleware: ['vite'],

    browserNoActivityTimeout: isCI ? 30000 : 0,

    browsers: ['ChromeHeadlessNoSandbox'],

    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-setuid-sandbox'],
      },
    },

    frameworks: ['mocha', 'vite'],

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
        resolve: {
          alias: Object.entries(mocks).map(([find, file]) => {
            const replacement = join(cwd, `test/mocks/${file}`);

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
  });
};
