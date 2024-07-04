import type { Viewport } from '@web/test-runner-commands/plugins';

export const viewports: Record<string, Viewport> = {
  default: { width: 1024, height: 768 },
  'screen-1440-900': { width: 1440, height: 900 },
};
