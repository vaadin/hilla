import { fileURLToPath } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import { rimraf } from 'rimraf';
import { RouteParamType } from '../../src/shared/routeParamType.js';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import createViewConfigJson from '../../src/vite-plugin/createViewConfigJson.js';
import { createTestingRouteFiles, createTestingRouteMeta, createTmpDir } from '../utils.js';

use(chaiAsPromised);

describe('@vaadin/hilla-file-router', () => {
  describe('generateJson', () => {
    let tmp: URL;
    let meta: RouteMeta;

    before(async () => {
      tmp = await createTmpDir();
      await createTestingRouteFiles(tmp);
    });

    after(async () => {
      await rimraf(fileURLToPath(tmp));
    });

    beforeEach(() => {
      meta = createTestingRouteMeta(tmp);
    });

    it('should generate a JSON representation of the route tree', async () => {
      await expect(createViewConfigJson(meta)).to.eventually.equal(
        JSON.stringify({
          route: '',
          params: {},
          children: [
            { route: 'about', title: 'About', params: {}, children: [] },
            {
              route: 'profile',
              params: {},
              children: [
                { route: '', title: 'Profile', params: {}, children: [] },
                {
                  route: 'account',
                  title: 'Account',
                  params: {},
                  children: [
                    {
                      route: 'security',
                      params: {},
                      children: [
                        { route: 'password', params: {}, title: 'Password', children: [] },
                        { route: 'two-factor-auth', params: {}, title: 'Two Factor Auth', children: [] },
                      ],
                    },
                  ],
                },
                {
                  route: 'friends',
                  params: {},
                  title: 'Friends Layout',
                  children: [
                    { route: 'list', title: 'List', params: {}, children: [] },
                    { route: ':user', title: 'User', params: { ':user': RouteParamType.Required }, children: [] },
                  ],
                },
              ],
            },
            {
              route: 'test',
              params: {},
              children: [
                {
                  route: ':optional?',
                  title: 'Optional',
                  params: { ':optional?': RouteParamType.Optional },
                  children: [],
                },
                { route: '*', title: 'Wildcard', params: { '*': RouteParamType.Wildcard }, children: [] },
              ],
            },
          ],
        }),
      );
    });
  });
});
