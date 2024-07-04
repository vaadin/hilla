/* eslint-disable tsdoc/syntax */
import { basename, extname } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { vitePlugin } from '@remcovaes/web-test-runner-vite-plugin';
import { chromeLauncher } from '@web/test-runner';
import { glob } from 'glob';
import type { UserConfigExport } from 'vite';
import viteConfig from './vite.config.js';

// eslint-disable-next-line no-undef
const cwd = pathToFileURL(`${process.cwd()}/`);

async function resolveViteConfig(cfg: UserConfigExport) {
  if (typeof cfg === 'function') {
    return await cfg({ command: 'serve', mode: 'development' });
  }

  return await cfg;
}

const cfg = await resolveViteConfig(viteConfig);

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  coverageConfig: {
    include: [fileURLToPath(new URL('src/**/*', cwd))],
    reportDir: fileURLToPath(new URL('.coverage/', cwd)),
  },
  groups: await glob('./test/**/*.{test,spec}.{ts,js,tsx,jsx}', { cwd }).then((files) =>
    files.map((file) => ({
      name: basename(file, extname(file)).replace(/\.spec|\.test/u, ''),
      files: fileURLToPath(new URL(file, cwd)),
    })),
  ),
  plugins: [vitePlugin(cfg)],
  browsers: [
    chromeLauncher({
      launchOptions: {
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
