import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

const customConfig: UserConfigFn = (env) => ({
  resolve: {
    preserveSymlinks: false,
  },
  server: {
    fs: { allow: [resolve('../../../../')] },
  },
  plugins: [react()],
});

export default overrideVaadinConfig(customConfig);
