/* eslint-disable tsdoc/syntax */
import { opendir } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { vitePlugin } from '@remcovaes/web-test-runner-vite-plugin';
import { chromeLauncher } from '@web/test-runner';

const root = new URL('./', import.meta.url);

/**
 * @type {string[]}
 */
const packages = await (async () => {
  const pkgs = [];
  const dir = await opendir(new URL('./packages/ts/', root));
  for await (const dirent of dir) {
    if (dirent.isDirectory()) {
      pkgs.push(dirent.name);
    }
  }
  return pkgs;
})();

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

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  coverageConfig: {
    include: [fileURLToPath(new URL('./src/**/*', cwd))],
    reportDir: fileURLToPath(new URL('./.coverage/', cwd)),
  },
  groups: packages.map((pkg) => ({
    name: pkg,
    files: fileURLToPath(new URL(`packages/ts/${pkg}/test/**/*.(test|spec).(ts|js|tsx|jsx)`, root)),
  })),
  plugins: [vitePlugin()],
  browsers: [
    chromeLauncher({
      launchOptions: {
        // eslint-disable-next-line no-undef
        executablePath: process.env.CHROME_BIN,
      },
    }),
  ],
};
