import { appendFile, mkdir, mkdtemp } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import deepEqualInAnyOrder from 'deep-equal-in-any-order';
import { rimraf } from 'rimraf';
import collectRoutes from '../src/collectRoutes.js';
import { createTestingRouteMeta } from './utils.js';

use(deepEqualInAnyOrder);

describe('@vaadin/hilla-file-router', () => {
  describe('collectFileRoutes', () => {
    const extensions = ['.tsx', '.jsx', '.ts', '.js'];
    let tmp: URL;

    before(async () => {
      tmp = pathToFileURL(`${await mkdtemp(join(tmpdir(), 'hilla-file-router-'))}/`);
    });

    after(async () => {
      await rimraf(fileURLToPath(tmp));
    });

    beforeEach(async () => {
      await rimraf(fileURLToPath(new URL('*', tmp)), { glob: true });
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

      await Promise.all([
        mkdir(new URL('profile/account/security/', tmp), { recursive: true }),
        mkdir(new URL('profile/friends/', tmp), { recursive: true }),
      ]);
      await Promise.all([
        appendFile(new URL('profile/account/account.layout.tsx', tmp), ''),
        appendFile(new URL('profile/account/security/password.jsx', tmp), ''),
        appendFile(new URL('profile/account/security/password.scss', tmp), ''),
        appendFile(new URL('profile/account/security/two-factor-auth.ts', tmp), ''),
        appendFile(new URL('profile/friends/friends.layout.tsx', tmp), ''),
        appendFile(new URL('profile/friends/list.js', tmp), ''),
        appendFile(new URL('profile/friends/{user}.tsx', tmp), ''),
        appendFile(new URL('profile/index.tsx', tmp), ''),
        appendFile(new URL('profile/index.css', tmp), ''),
        appendFile(new URL('about.tsx', tmp), ''),
      ]);

      const result = await collectRoutes(tmp, { extensions });

      expect(result).to.deep.equals(createTestingRouteMeta(tmp));
    });
  });
});
