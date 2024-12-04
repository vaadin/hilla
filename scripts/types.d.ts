/* eslint-disable import/unambiguous */

declare module '@remcovaes/web-test-runner-vite-plugin' {
  import type { Plugin, UserConfig } from 'vite';

  export function vitePlugin(config?: UserConfig): Plugin;
  export function removeViteLogging(): (message: string) => boolean;
}
