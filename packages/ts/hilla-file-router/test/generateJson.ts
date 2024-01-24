import { expect } from '@esm-bundle/chai';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import type { RouteMeta } from '../src/collectRoutes.js';
import generateJson from '../src/generateJson.js';
import { createTestingRouteMeta } from './utils.js';

describe('@vaadin/hilla-file-router', () => {
  describe('generateJson', () => {
    let meta: RouteMeta;

    beforeEach(() => {
      const dir = pathToFileURL(join(tmpdir(), 'hilla-file-router/'));
      meta = createTestingRouteMeta(new URL('./views/', dir));
    });

    it('should generate a JSON representation of the route tree', () => {
      const generated = generateJson(meta);

      expect(generated).to.equal(`[
  "/profile/",
  "/profile/friends/list",
  "/profile/friends/[user]",
  "/profile/account/security/password",
  "/profile/account/security/two-factor-auth",
  "/about"
]`);
    });
  });
});
