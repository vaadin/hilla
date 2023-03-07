const { resolve } = require('node:path');
const { parseArgs } = require('node:util');
const karmaChromeLauncher = require('karma-chrome-launcher');
const karmaCoverage = require('karma-coverage');
const karmaMocha = require('karma-mocha');
const karmaVite = require('karma-vite');
const puppeteer = require('puppeteer');

process.env.CHROME_BIN = puppeteer.executablePath();

const cwd = process.cwd();

const { values } = parseArgs({
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
const { coverage, watch: _watch } = values;
const watch = _watch && !isCI;

module.exports = (config) => {
  config.set({
    plugins: [karmaMocha, karmaChromeLauncher, karmaVite, karmaCoverage],

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
    ],

    reporters: ['progress', coverage && 'coverage'].filter(Boolean),

    autoWatch: watch,
    singleRun: !watch,

    coverageReporter: {
      dir: '.coverage/',
      reporters: [!isCI && { type: 'html', subdir: 'html' }, { type: 'lcovonly', subdir: '.' }].filter(Boolean),
    },
  });
};
