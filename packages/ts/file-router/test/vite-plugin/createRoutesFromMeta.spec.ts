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
      meta = [
        ...meta,
        {
          path: 'issue-2928-flow-auto-layout',
          file: new URL('issue-2928-flow-auto-layout.tsx', dir),
          flowLayout: true,
        },
        {
          path: 'mod-extension-only',
          flowLayout: true,
          children: [
            {
              path: 'mod-extension-only-child',
              file: new URL('mod-extension-only-child.tsx', dir),
            },
          ],
        },
      ];
      const generated = createRoutesFromMeta(meta, runtimeUrls);

      expect(generated).to
        .equal(`import { createRoute as createRoute_1, extendModule as extendModule_1 } from "@vaadin/hilla-file-router/runtime.js";
import type { AgnosticRoute as AgnosticRoute_1 } from "@vaadin/hilla-file-router/types.js";
import * as Page_1 from "../issue-2928-flow-auto-layout.js";
import * as Page_2 from "../views/nameToReplace.js";
import * as Page_3 from "../views/profile/@index.js";
import * as Layout_1 from "../views/profile/account/@layout.js";
import * as Page_4 from "../views/profile/account/security/password.js";
import * as Page_5 from "../views/profile/account/security/two-factor-auth.js";
import * as Page_6 from "../views/profile/friends/{user}.js";
import * as Layout_2 from "../views/profile/friends/@layout.js";
import * as Page_7 from "../views/profile/friends/list.js";
import * as Page_8 from "../views/test/{...wildcard}.js";
import * as Page_9 from "../views/test/{{optional}}.js";
import * as Page_10 from "../views/test/issue-002378/{requiredParam}/edit.js";
import * as Layout_3 from "../views/test/issue-002571-empty-layout/@layout.js";
import * as Page_11 from "../views/test/issue-002879-config-below.js";
const routes: readonly AgnosticRoute_1[] = [
    createRoute_1("nameToReplace", Page_2),
    createRoute_1("profile", [
        createRoute_1("", Page_3),
        createRoute_1("account", Layout_1, [
            createRoute_1("security", [
                createRoute_1("password", Page_4),
                createRoute_1("two-factor-auth", Page_5)
            ])
        ]),
        createRoute_1("friends", Layout_2, [
            createRoute_1("list", Page_7),
            createRoute_1(":user", Page_6)
        ])
    ]),
    createRoute_1("test", [
        createRoute_1(":optional?", Page_9),
        createRoute_1("*", Page_8),
        createRoute_1("issue-002378", [
            createRoute_1(":requiredParam", [
                createRoute_1("edit", Page_10)
            ])
        ]),
        createRoute_1("issue-002571-empty-layout", Layout_3, []),
        createRoute_1("issue-002879-config-below", Page_11)
    ]),
    createRoute_1("issue-2928-flow-auto-layout", extendModule_1(Page_1, { "flowLayout": true })),
    createRoute_1("mod-extension-only", extendModule_1(null, { "flowLayout": true }), [
        createRoute_1("mod-extension-only-child", Page_2)
    ])
];
export default routes;
`);
    });

    it('should generate an empty list when no routes are found', () => {
      const generated = createRoutesFromMeta([], runtimeUrls);

      expect(generated).to
        .equal(`import type { AgnosticRoute as AgnosticRoute_1 } from "@vaadin/hilla-file-router/types.js";
const routes: readonly AgnosticRoute_1[] = [];
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
