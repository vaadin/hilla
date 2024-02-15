import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { expect } from '@esm-bundle/chai';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import createRoutesFromMeta from '../../src/vite-plugin/createRoutesFromMeta.js';
import { createTestingRouteMeta } from '../utils.js';

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

      expect(generated).to.equal(`import * as Page0 from "../views/nameToReplace.js";
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
            path: Page0.config?.route ?? "nameToReplace",
            module: Page0
        }, {
            path: "profile",
            children: [{
                    path: Page1.config?.route ?? "",
                    module: Page1
                }, {
                    path: Layout5.config?.route ?? "account",
                    module: Layout5,
                    children: [{
                            path: "security",
                            children: [{
                                    path: Page2.config?.route ?? "password",
                                    module: Page2
                                }, {
                                    path: Page3.config?.route ?? "two-factor-auth",
                                    module: Page3
                                }],
                        }],
                }, {
                    path: Layout8.config?.route ?? "friends",
                    module: Layout8,
                    children: [{
                            path: Page6.config?.route ?? "list",
                            module: Page6
                        }, {
                            path: Page7.config?.route ?? ":user",
                            module: Page7
                        }],
                }],
        }, {
            path: "test",
            children: [{
                    path: "*",
                }, {
                    path: ":optional?",
                }],
        }],
};
export default routes;
`);
    });
  });
});
