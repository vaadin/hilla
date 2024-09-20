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
        layouts: new URL('generated/layouts.json', dir),
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
import * as Page12 from "../views/test/issue-002378/{requiredParam}/edit.js";
import * as Layout15 from "../views/test/issue-002571-empty-layout/@layout.js";
import * as Page16 from "../views/test/issue-002879-config-below.js";
const routes: readonly AgnosticRoute[] = [
    createRoute("nameToReplace", false, Page0),
    createRoute("profile", false, [
        createRoute("", false, Page1),
        createRoute("account", false, Layout5, [
            createRoute("security", false, [
                createRoute("password", false, Page2),
                createRoute("two-factor-auth", false, Page3)
            ])
        ]),
        createRoute("friends", false, Layout8, [
            createRoute("list", false, Page6),
            createRoute(":user", false, Page7)
        ])
    ]),
    createRoute("test", false, [
        createRoute(":optional?", false, Page10),
        createRoute("*", false, Page11),
        createRoute("issue-002378", false, [
            createRoute(":requiredParam", false, [
                createRoute("edit", false, Page12)
            ])
        ]),
        createRoute("issue-002571-empty-layout", false, Layout15, []),
        createRoute("issue-002879-config-below", false, Page16)
    ])
];
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
