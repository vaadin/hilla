import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { expect } from '@esm-bundle/chai';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import createRoutesFromMeta from '../../src/vite-plugin/createRoutesFromMeta.js';
import type { RuntimeFileUrls } from '../../src/vite-plugin/generateRuntimeFiles.js';
import { createTestingRouteMeta } from '../utils.js';

describe('@vaadin/hilla-file-router', () => {
  describe('generateRoutes', () => {
    let dir: URL;
    let meta: readonly RouteMeta[];
    let runtimeUrls: RuntimeFileUrls;

    beforeEach(() => {
      dir = pathToFileURL(join(tmpdir(), 'file-router/'));
      meta = createTestingRouteMeta(new URL('./views/', dir));
      runtimeUrls = {
        json: new URL('server/file-routes.json', dir),
        code: new URL('generated/file-routes.ts', dir),
      };
    });

    it('should generate a framework-agnostic tree of routes', () => {
      const generated = createRoutesFromMeta(meta, runtimeUrls);

      expect(generated).to.equal(`import type { AgnosticRoute } from "@vaadin/hilla-file-router/types.js";
import { createRoute } from "@vaadin/hilla-file-router/runtime.js";
import * as Page0 from "../views/nameToReplace.js";
import * as Page1 from "../views/profile/@index.js";
import * as Page2 from "../views/profile/account/security/password.js";
import * as Page3 from "../views/profile/account/security/two-factor-auth.js";
import * as Layout5 from "../views/profile/account/@layout.js";
import * as Page6 from "../views/profile/friends/list.js";
import * as Page7 from "../views/profile/friends/{user}.js";
import * as Layout8 from "../views/profile/friends/@layout.js";
import * as Page10 from "../views/test/{{optional}}.js";
import * as Page11 from "../views/test/{...wildcard}.js";
const routes: readonly AgnosticRoute[] = [createRoute("nameToReplace", Page0), createRoute("profile", [createRoute("", Page1), createRoute("account", Layout5, [createRoute("security", [createRoute("password", Page2), createRoute("two-factor-auth", Page3)])]), createRoute("friends", Layout8, [createRoute("list", Page6), createRoute(":user", Page7)])]), createRoute("test", [createRoute(":optional?", Page10), createRoute("*", Page11)])];
export default routes;
`);
    });

    it('should generate an empty list when no routes are found', () => {
      const generated = createRoutesFromMeta([], runtimeUrls);

      expect(generated).to.equal(`import type { AgnosticRoute } from "@vaadin/hilla-file-router/types.js";
const routes: readonly AgnosticRoute[] = [];
export default routes;
`);
    });

    it('should add console.error calls for duplicated paths', () => {
      const metaWithDuplicatedPaths = [
        ...meta,
        {
          path: 'profile',
          file: new URL('profile/@index.tsx', dir),
          children: [],
        },
      ];
      const generated = createRoutesFromMeta(metaWithDuplicatedPaths, runtimeUrls);
      expect(generated).to.contain('console.error("Two views share the same path: profile");');
    });
  });
});
