const { resolve } = require('node:path');
const { parseArgs } = require('node:util');
const karmaChromeLauncher = require('karma-chrome-launcher');
const karmaCoverage = require('karma-coverage');
const karmaMocha = require('karma-mocha');
const karmaSpecReporter = require('karma-spec-reporter');
const karmaVite = require('karma-vite');

// The current package, one of the packages in the `packages` dir
const cwd = process.cwd();

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
  config.set({
    plugins: [karmaMocha, karmaChromeLauncher, karmaVite, karmaCoverage, karmaSpecReporter],

    browserNoActivityTimeout : isCI ? 30000 : 0,

    browsers: ['ChromeHeadlessNoSandbox'],

    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-setuid-sandbox'],
      },
    },

    frameworks: ['vite', 'mocha'],

    files: [
      {
        pattern: resolve(cwd, 'test/**/*.test.ts'),
        type: 'module',
        watched: false,
        served: false,
      },
      {
        pattern: resolve(cwd, 'test/**/*.spec.ts'),
        type: 'module',
        watched: false,
        served: false,
      },
      {
        pattern: resolve(cwd, 'test/**/*.spec.tsx'),
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
      config: {
        cacheDir: resolve(cwd, '.vite'),
      }
    }
  });
};
