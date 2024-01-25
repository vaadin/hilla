import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { expect } from '@esm-bundle/chai';
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

      expect(generated).to.equal(`import Page0 from "../views/about.tsx";
import Page1 from "../views/profile/index.tsx";
import Page2 from "../views/profile/account/security/password.jsx";
import Page3 from "../views/profile/account/security/two-factor-auth.ts";
import Layout5 from "../views/profile/account/account.layout.tsx";
import Page6 from "../views/profile/friends/list.js";
import Page7 from "../views/profile/friends/{user}.tsx";
import Layout8 from "../views/profile/friends/friends.layout.tsx";
const routes = {
    path: "",
    children: [{
            path: "about",
            component: Page0
        }, {
            path: "profile",
            children: [{
                    path: "",
                    component: Page1
                }, {
                    path: "account",
                    component: Layout5,
                    children: [{
                            path: "security",
                            children: [{
                                    path: "password",
                                    component: Page2
                                }, {
                                    path: "two-factor-auth",
                                    component: Page3
                                }],
                        }],
                }, {
                    path: "friends",
                    component: Layout8,
                    children: [{
                            path: "list",
                            component: Page6
                        }, {
                            path: ":user",
                            component: Page7
                        }],
                }],
        }],
};
export default routes;
`);
    });
  });
});
