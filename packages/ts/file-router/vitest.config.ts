import { mergeConfig } from 'vitest/config';
import sharedConfig, { cwd } from '../../../scripts/vite/node.vitest.config.js';
import { createResolver } from '../../../scripts/vite/test-utils.js';

export default mergeConfig(
  sharedConfig,
  createResolver(
    {
      '@vaadin/hilla-react-auth': './vaadin-hilla-react-auth.ts',
      'react-router-dom': './react-router-dom.ts',
    },
    cwd,
  ),
);
