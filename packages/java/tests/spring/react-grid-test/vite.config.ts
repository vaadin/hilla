import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated.ts';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/
});

export default overrideVaadinConfig(customConfig);
