import { appendFile, mkdir, rm } from 'node:fs/promises';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import chaiLike from 'chai-like';
import { RouteParamType } from '../../src/shared/routeParamType.js';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import createViewConfigJson from '../../src/vite-plugin/createViewConfigJson.js';
import { createTestingRouteFiles, createTestingRouteMeta, createTmpDir } from '../utils.js';

use(chaiLike);
use(chaiAsPromised);

describe('@vaadin/hilla-file-router', () => {
  describe('createViewConfigJson', () => {
    let tmp: URL;
    let meta: readonly RouteMeta[];
    let layoutOnlyDir: URL;
    let layoutOnlyDirLayout: URL;

    before(async () => {
      tmp = await createTmpDir();

      layoutOnlyDir = new URL('layout-only/', tmp);
      layoutOnlyDirLayout = new URL('@layout.tsx', layoutOnlyDir);

      await createTestingRouteFiles(tmp);
      await mkdir(layoutOnlyDir, { recursive: true });
      await appendFile(layoutOnlyDirLayout, 'export default function LayoutOnly() {};');
    });

    after(async () => {
      await rm(tmp, { recursive: true, force: true });
    });

    beforeEach(() => {
      meta = [
        ...createTestingRouteMeta(tmp),
        {
          path: 'layout-only',
          layout: layoutOnlyDirLayout,
          children: undefined,
        },
      ];
    });

    it('should generate a JSON representation of the route tree', async () => {
      await expect(createViewConfigJson(meta).then(JSON.parse)).to.eventually.be.like([
        { route: 'about', title: 'About', params: {} },
        {
          route: 'profile',
          params: {},
          children: [
            { route: '', title: 'Profile', params: {} },
            {
              route: 'account',
              title: 'Account',
              params: {},
              children: [
                {
                  route: 'security',
                  params: {},
                  children: [
                    { route: 'password', params: {}, title: 'Password' },
                    { route: 'two-factor-auth', params: {}, title: 'Two Factor Auth' },
                  ],
                },
              ],
            },
            {
              route: 'friends',
              params: {},
              title: 'Friends Layout',
              children: [
                { route: 'list', params: {}, title: 'List' },
                { route: ':user', params: { ':user': RouteParamType.Required }, title: 'User' },
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
            },
            { route: '*', title: 'Wildcard', params: { '*': RouteParamType.Wildcard } },
          ],
        },
        {
          route: 'layout-only',
          params: {},
          title: 'Layout Only',
          children: [],
        },
      ]);
    });
  });
});
