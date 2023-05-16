import react from '@vitejs/plugin-react';
import type { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/
  plugins: [
    react({
      include: '**/*.tsx',
    }),
  ],
});

export default overrideVaadinConfig(customConfig);
