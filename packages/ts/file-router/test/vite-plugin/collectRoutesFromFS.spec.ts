import { fileURLToPath } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import { rimraf } from 'rimraf';
import sinonChai from 'sinon-chai';
import type { Writable } from 'type-fest';
import type { Logger } from 'vite';
import collectRoutesFromFS, { type RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import { createLogger, createTestingRouteFiles, createTestingRouteMeta, createTmpDir } from '../utils.js';

use(chaiAsPromised);
use(sinonChai);

const collator = new Intl.Collator('en-US');

function cleanupRouteMeta(route: Writable<RouteMeta>): void {
  if (!route.file) {
    delete route.file;
  }

  if (!route.layout) {
    delete route.layout;
  }

  (route.children as RouteMeta[] | undefined)
    ?.sort(({ path: a }, { path: b }) => collator.compare(a, b))
    .forEach(cleanupRouteMeta);
}

describe('@vaadin/hilla-file-router', () => {
  describe('collectRoutesFromFS', () => {
    const extensions = ['.tsx', '.jsx'];
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
      routes.forEach(cleanupRouteMeta);

      const expected = createTestingRouteMeta(tmp);
      expected.forEach(cleanupRouteMeta);

      expect(routes).to.deep.equal(expected);
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(logger.error).to.be.calledOnceWithExactly(
        `The file "${new URL('./test/no-default-export.tsx', tmp).toString()}" should contain a default export of a component`,
      );
    });
  });
});
