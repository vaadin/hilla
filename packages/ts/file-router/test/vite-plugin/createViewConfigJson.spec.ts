import { appendFile, mkdir, rm } from 'node:fs/promises';
import chaiAsPromised from 'chai-as-promised';
import chaiLike from 'chai-like';
import { afterAll, beforeAll, beforeEach, chai, describe, expect, it } from 'vitest';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import createViewConfigJson from '../../src/vite-plugin/createViewConfigJson.js';
import { createTestingRouteFiles, createTestingRouteMeta, createTmpDir } from '../utils.js';

chai.use(chaiAsPromised);
chai.use(chaiLike);

describe('@vaadin/hilla-file-router', () => {
  describe('createViewConfigJson', () => {
    let tmp: URL;
    let meta: readonly RouteMeta[];
    let layoutOnlyDir: URL;
    let layoutOnlyDirLayout: URL;

    beforeAll(async () => {
      tmp = await createTmpDir();

      layoutOnlyDir = new URL('layout-only/', tmp);
      layoutOnlyDirLayout = new URL('@layout.tsx', layoutOnlyDir);

      await createTestingRouteFiles(tmp);
      await mkdir(layoutOnlyDir, { recursive: true });
      await appendFile(layoutOnlyDirLayout, 'export default function LayoutOnly() {};');
    });

    afterAll(async () => {
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
      const config = JSON.stringify(await createViewConfigJson(meta), null, 2);
      await expect(config).toMatchFileSnapshot('fixtures/createViewConfigJson/view-config.snap.json');
    });

    it.each([
      ['the root directory', (index: URL): RouteMeta[] => [{ path: '', file: index }]],
      [
        'a subdirectory with a layout',
        (index: URL): RouteMeta[] => [
          { path: 'layout-only', layout: layoutOnlyDirLayout, children: [{ path: '', file: index }] },
        ],
      ],
      [
        'a nested subdirectory without a layout',
        (index: URL): RouteMeta[] => [
          { path: 'parent', children: [{ path: 'child', children: [{ path: '', file: index }] }] },
        ],
      ],
    ])('should throw if an "@index" view in %s overrides its route', async (_, createMeta) => {
      const indexFile = new URL('@index.tsx', layoutOnlyDir);
      await appendFile(
        indexFile,
        "export const config = { route: 'landing' };\nexport default function LayoutOnlyIndex() {};",
      );

      try {
        await expect(createViewConfigJson(createMeta(indexFile))).to.be.rejectedWith(
          'is an "@index" view and cannot override its route with "config.route"',
        );
      } finally {
        await rm(indexFile);
      }
    });

    it('should accept an empty route in an "@index" view', async () => {
      const indexFile = new URL('@index.tsx', layoutOnlyDir);
      await appendFile(
        indexFile,
        "export const config = { route: '' };\nexport default function LayoutOnlyIndex() {};",
      );

      try {
        expect(await createViewConfigJson([{ path: '', file: indexFile }])).to.be.like([{ route: '' }]);
      } finally {
        await rm(indexFile);
      }
    });
  });
});
