import { fileURLToPath } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import deepEqualInAnyOrder from 'deep-equal-in-any-order';
import { rimraf } from 'rimraf';
import collectRoutesFromFS from '../../src/vite-plugin/collectRoutesFromFS.js';
import { createTestingRouteFiles, createTestingRouteMeta, createTmpDir } from '../utils.js';

use(deepEqualInAnyOrder);

describe('@vaadin/hilla-file-router', () => {
  describe('collectFileRoutes', () => {
    const extensions = ['.tsx', '.jsx', '.ts', '.js'];
    let tmp: URL;

    before(async () => {
      tmp = await createTmpDir();
      await createTestingRouteFiles(tmp);
    });

    after(async () => {
      await rimraf(fileURLToPath(tmp));
    });

    it('should build a route tree', async () => {
      // root
      // ├── profile
      // │   ├── account
      // │   │   ├── layout.tsx
      // │   │   └── security
      // │   │       ├── password.tsx
      // │   │       └── two-factor-auth.tsx
      // │   ├── friends
      // │   │   ├── layout.tsx
      // │   │   ├── list.tsx
      // │   │   └── {user}.tsx
      // │   ├── index.tsx
      // │   └── layout.tsx
      // └── about.tsx
      const result = await collectRoutesFromFS(tmp, { extensions });

      expect(result).to.deep.equals(createTestingRouteMeta(tmp));
    });
  });
});
