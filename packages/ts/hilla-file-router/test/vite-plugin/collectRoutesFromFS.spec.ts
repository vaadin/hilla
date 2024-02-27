import { fileURLToPath } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import deepEqualInAnyOrder from 'deep-equal-in-any-order';
import { rimraf } from 'rimraf';
import type { Logger } from 'vite';
import collectRoutesFromFS from '../../src/vite-plugin/collectRoutesFromFS.js';
import { createLogger, createTestingRouteFiles, createTestingRouteMeta, createTmpDir } from '../utils.js';

use(deepEqualInAnyOrder);
use(chaiAsPromised);

describe('@vaadin/hilla-file-router', () => {
  describe('collectFileRoutes', () => {
    const extensions = ['.tsx', '.jsx', '.ts', '.js'];
    let tmp: URL;
    let logger: Logger;

    before(async () => {
      tmp = await createTmpDir();
      await createTestingRouteFiles(tmp);
    });

    after(async () => {
      await rimraf(fileURLToPath(tmp));
    });

    beforeEach(() => {
      logger = createLogger();
    });

    it('should build a route tree', async () => {
      const routes = await collectRoutesFromFS(tmp, { extensions, logger });
      const expected = createTestingRouteMeta(tmp);
      expect(routes).to.deep.equalInAnyOrder(expected);
    });
  });
});
