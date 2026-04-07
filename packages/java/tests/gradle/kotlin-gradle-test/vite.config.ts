import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';

const customConfig: UserConfigFn = (env) => ({
  build: {
    rolldownOptions: {
      output: {
        // Work around a Rolldown bug where minified identifier names in a
        // dynamic-import chunk collide with cross-chunk runtime bindings,
        // causing "TypeError: t is not a function" at runtime.
        manualChunks(id: string) {
          if (id.includes('/markdown/src/markdown-helpers')) {
            return 'indexhtml';
          }
        },
      },
    },
  },
});

export default overrideVaadinConfig(customConfig);
