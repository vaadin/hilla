import { appendFile } from 'fs/promises';
import { mkdir, rm } from 'node:fs/promises';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
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
      await rm(tmp, { recursive: true, force: true });
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

    describe('failure cases', () => {
      let failureDir: URL;
      let internalDir: URL;

      beforeEach(async () => {
        failureDir = new URL('./failure/', tmp);
        internalDir = new URL('./internal/', failureDir);
        await mkdir(internalDir, { recursive: true });
        await appendFile(new URL('./@index.tsx', internalDir), 'export default function FailureInternalIndex() {}');
      });

      afterEach(async () => {
        await rm(failureDir, { recursive: true, force: true });
      });

      it('should throw an error if file starts with "@" and is not an "@index" or "@layout"', async () => {
        await Promise.all([
          appendFile(new URL('./@error.tsx', failureDir), 'export default function FailureError() {}'),
        ]);

        await expect(collectRoutesFromFS(tmp, { extensions, logger })).to.be.rejectedWith(
          'Symbol "@" is reserved for special directories and files; only "@layout" and "@index" are allowed',
        );
      });

      it('should throw an error if there is file and directory with the same name', async () => {
        await Promise.all([
          appendFile(new URL('./internal.tsx', failureDir), 'export default function FailureFileAndDir() {}'),
        ]);

        await expect(collectRoutesFromFS(tmp, { extensions, logger })).to.be.rejectedWith(
          'You cannot create a file and a directory with the same name. Use `@index` instead.',
        );
      });
    });
  });
});
