import { fileURLToPath } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import { rimraf } from 'rimraf';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import createViewConfigJson from '../../src/vite-plugin/createViewConfigJson.js';
import { createTestingRouteFiles, createTestingRouteMeta, createTestingViewMap, createTmpDir } from '../utils.js';

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
      await expect(createViewConfigJson(meta)).to.eventually.equal(JSON.stringify(createTestingViewMap()));
    });
  });
});
