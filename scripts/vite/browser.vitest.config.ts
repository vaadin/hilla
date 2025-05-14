/// <reference types="vitest/node" />
import { parseArgs } from 'node:util';
import react from '@vitejs/plugin-react';
import { mergeConfig, type ViteUserConfig } from 'vitest/config';
import type { BrowserProviderOptions } from 'vitest/node';
import nodeConfig, { isCI, packageJson, root, cwd } from './node.vitest.config.js';
import { constructCss } from './plugins.js';

export { root, cwd, isCI, packageJson };

const {
  values: { inspect },
} = parseArgs({
  options: {
    inspect: {
      type: 'boolean',
    },
  },
  strict: false,
});

function getBrowserProviderOptions(): BrowserProviderOptions {
  const { PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD, CHROME_BIN } = process.env;

  if (PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD === '1') {
    if (typeof CHROME_BIN === 'string') {
      return {
        launch: {
          executablePath: CHROME_BIN,
        },
      };
    }

    throw new Error(
      'You have to set CHROME_BIN along with PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD to make tests working,' +
        'or disable PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD to use browser provided by Playwright',
    );
  }

  return {};
}

export default mergeConfig(nodeConfig, {
  plugins: [
    constructCss(),
    react({
      include: '**/*.tsx',
      babel: {
        plugins: [['module:@preact/signals-react-transform', { mode: 'all' }]],
      },
    }),
  ],
  server: {
    warmup: {
      clientFiles: ['src/**/*', 'test/**/*'],
    },
  },
  test: {
    browser: {
      api: {
        port: 9876,
      },
      ui: !isCI,
      screenshotFailures: isCI,
      provider: 'playwright',
      enabled: true,
      headless: !inspect,
      instances: [
        {
          browser: 'chromium',
          ...getBrowserProviderOptions(),
        },
      ],
    },
  },
}) satisfies ViteUserConfig;
