import { mergeConfig } from 'vitest/config';
import sharedConfig from '../../../scripts/vite/browser.vitest.config.js';

export default mergeConfig(sharedConfig, {
  test: {
    // TODO: Remove this option when all errors are properly handled
    dangerouslyIgnoreUnhandledErrors: true,
  },
});
