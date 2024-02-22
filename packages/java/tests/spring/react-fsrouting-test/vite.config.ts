import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';
import vitePluginFileSystemRouter from '@vaadin/hilla-file-router/vite-plugin.js';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/
  plugins: [
    vitePluginFileSystemRouter()
  ],
});

export default overrideVaadinConfig(customConfig);
