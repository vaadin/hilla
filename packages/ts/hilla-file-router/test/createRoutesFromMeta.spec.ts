import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { expect } from '@esm-bundle/chai';
import type { RouteMeta } from '../src/vite-plugin/collectRoutesFromFS.js';
import createRoutesFromMeta from '../src/vite-plugin/createRoutesFromMeta.js';
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
      const generated = createRoutesFromMeta(meta, new URL('./out/', dir));

      expect(generated).to.equal(`import * as Page0 from "../views/about.js";
import * as Page1 from "../views/profile/$index.js";
import * as Page2 from "../views/profile/account/security/password.js";
import * as Page3 from "../views/profile/account/security/two-factor-auth.js";
import * as Layout5 from "../views/profile/account/$layout.js";
import * as Page6 from "../views/profile/friends/list.js";
import * as Page7 from "../views/profile/friends/{user}.js";
import * as Layout8 from "../views/profile/friends/$layout.js";
const routes = {
    path: "",
    children: [{
            path: "about",
            module: Page0
        }, {
            path: "profile",
            children: [{
                    path: "",
                    module: Page1
                }, {
                    path: "account",
                    module: Layout5,
                    children: [{
                            path: "security",
                            children: [{
                                    path: "password",
                                    module: Page2
                                }, {
                                    path: "two-factor-auth",
                                    module: Page3
                                }],
                        }],
                }, {
                    path: "friends",
                    module: Layout8,
                    children: [{
                            path: "list",
                            module: Page6
                        }, {
                            path: ":user",
                            module: Page7
                        }],
                }],
        }],
};
export default routes;
`);
    });
  });
});
