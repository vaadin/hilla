import { fileURLToPath } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import { rimraf } from 'rimraf';
import type { Writable } from 'type-fest';
import type { Logger } from 'vite';
import collectRoutesFromFS from '../../src/vite-plugin/collectRoutesFromFS.js';
import type { RouteMeta } from '../../vite-plugin/collectRoutesFromFS.js';
import { createLogger, createTestingRouteFiles, createTestingRouteMeta, createTmpDir } from '../utils.js';

use(chaiAsPromised);

const collator = new Intl.Collator('en-US');

function cleanupRouteMeta(route: Writable<RouteMeta>): void {
  if (!route.file) {
    delete route.file;
  }

  if (!route.layout) {
    delete route.layout;
  }

  route.children.sort(({ path: a }, { path: b }) => collator.compare(a, b)).forEach(cleanupRouteMeta);
}

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
      cleanupRouteMeta(routes);

      const expected = createTestingRouteMeta(tmp);
      cleanupRouteMeta(expected);

      expect(routes).to.deep.equal(expected);
    });
  });
});
