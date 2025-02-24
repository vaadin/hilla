import { mergeConfig } from 'vitest/config';
import sharedConfig, { cwd } from '../../../scripts/vite/browser.vitest.config.js';
import { createResolver } from '../../../scripts/vite/test-utils.js';

export default mergeConfig(
  sharedConfig,
  createResolver(
    {
      './FluxConnection.js': './FluxConnection.ts',
      'atmosphere.js': './atmosphere.ts',
    },
    cwd,
  ),
);
