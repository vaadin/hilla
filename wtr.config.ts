import { fileURLToPath, pathToFileURL } from 'node:url';
import { parseArgs } from 'node:util';
import { vitePlugin, removeViteLogging } from '@remcovaes/web-test-runner-vite-plugin';
import { chromeLauncher } from '@web/test-runner';
import viteConfig from './vite.config.js';

const cwd = pathToFileURL(`${process.cwd()}/`);

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

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  concurrency: 10,
  // nodeResolve: true,
  watch,
  coverage,
  rootDir: fileURLToPath(cwd),
  files: 'test/**/*.spec.{ts,tsx}',
  coverageConfig: {
    include: [fileURLToPath(new URL('src/**/*', cwd))],
    reportDir: fileURLToPath(new URL('.coverage/', cwd)),
  },
  plugins: [vitePlugin(viteConfig)],
  filterBrowserLogs: removeViteLogging,
  browsers: [
    chromeLauncher({
      launchOptions: {
        // Chrome cannot create its sandbox on CI runners that restrict
        // unprivileged user namespaces, the same as in karma.config.cjs.
        args: isCI ? ['--no-sandbox', '--disable-setuid-sandbox'] : [],
        // eslint-disable-next-line no-undef
        executablePath: process.env.CHROME_BIN,
      },
    }),
  ],
  testFramework: {
    config: {
      ui: 'bdd',
      timeout: '4000',
    },
  },
};
