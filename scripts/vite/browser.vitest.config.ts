// eslint-disable-next-line @typescript-eslint/triple-slash-reference,spaced-comment
/// <reference types="vitest/node" />
import react from '@vitejs/plugin-react';
import { mergeConfig, type ViteUserConfig } from 'vitest/config';
import nodeConfig, { isCI, packageJson, root, cwd } from './node.vitest.config.js';
import { constructCss } from './plugins.js';

export { root, cwd, isCI, packageJson };

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
      headless: true,
      instances: [
        {
          browser: 'chromium',
          launch: {
            executablePath: process.env.CHROME_BIN,
          },
        },
      ],
    },
  },
}) satisfies ViteUserConfig;
