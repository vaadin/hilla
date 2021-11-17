/* eslint-disable @typescript-eslint/no-var-requires,import/no-extraneous-dependencies */
import { esbuildPlugin } from '@web/dev-server-esbuild';
import { chromeLauncher } from '@web/test-runner-chrome';
import { readdir } from 'fs/promises';
import { URL } from 'url';

// One of the packages in the `packages` dir
const cwd = process.cwd();

// The root project directory
const dir = new URL('./packages/ts', import.meta.url);

const packages = await readdir(dir);
const index = packages.findIndex((pack) => cwd.endsWith(pack));

const tsExtPattern = /\.ts$/;

export default {
  rootDir: '.',
  nodeResolve: true,
  // necessary to avoid "address already in use :::8000" error in CI
  port: 8000 + index,
  browserStartTimeout: 60000, // default 30000
  testsStartTimeout: 60000, // default 10000
  testsFinishTimeout: 60000, // default 20000
  preserveSymlinks: true,
  plugins: [
    esbuildPlugin({ ts: true, target: 'auto' }),
    {
      name: 'fix-node-resolve-issue',
      transformImport({ source }) {
        return source.includes('wds-outside-root') && source.endsWith('.ts')
          ? source.replace(tsExtPattern, '.js')
          : source;
      },
    },
  ],
  browsers: [
    chromeLauncher({
      launchOptions: {
        args: ['--no-sandbox', '--disable-setuid-sandbox'],
      },
    }),
  ],
  coverageConfig: {
    report: true,
    reportDir: '.coverage',
    threshold: {
      statements: 70,
      branches: 70,
      functions: 70,
      lines: 70,
    },
  },
};
