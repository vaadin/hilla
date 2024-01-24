import { expect } from '@esm-bundle/chai';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import type { RouteMeta } from '../src/collectRoutes.js';
import generateRoutes from '../src/generateRoutes.js';
import { createTestingRouteMeta } from './utils.js';

describe('@vaadin/hilla-file-router', () => {
  describe('generateRoutes', () => {
    let dir: URL;
    let meta: RouteMeta;

    beforeEach(() => {
      dir = pathToFileURL(join(tmpdir(), 'hilla-file-router/'));
      meta = createTestingRouteMeta(new URL('./views/', dir));
    });

    it('should generate a framework-agnostic tree of routes', () => {
      const generated = generateRoutes(meta, new URL('./out/', dir));

      expect(generated).to.equal(`import Page0, { meta0 } from "../views/profile/friends/list.tsx";
import Page1, { meta1 } from "../views/profile/friends/[user].tsx";
import Layout2, { meta2 } from "../views/profile/friends/friends.layout.tsx";
import Page3, { meta3 } from "../views/profile/account/security/password.tsx";
import Page4, { meta4 } from "../views/profile/account/security/two-factor-auth.tsx";
import Layout6, { meta6 } from "../views/account.layout.tsx";
import Page8, { meta8 } from "../views/about.tsx";
const routes = {
    path: "",
    meta: meta9,
    children: [{
            path: "profile",
            meta: meta7,
            children: [{
                    path: "friends",
                    component: Layout2,
                    meta: meta2,
                    children: [{
                            path: "list",
                            component: Page0,
                            meta: meta0,
                        }, {
                            path: ":user",
                            component: Page1,
                            meta: meta1,
                        }],
                }, {
                    path: "account",
                    component: Layout6,
                    meta: meta6,
                    children: [{
                            path: "security",
                            meta: meta5,
                            children: [{
                                    path: "password",
                                    component: Page3,
                                    meta: meta3,
                                }, {
                                    path: "two-factor-auth",
                                    component: Page4,
                                    meta: meta4,
                                }],
                        }],
                }],
        }, {
            path: "about",
            component: Page8,
            meta: meta8,
        }],
};
export default routes;
`);
    });
  });
});
