import { appendFile, mkdir, rm } from 'node:fs/promises';
import chaiLike from 'chai-like';
import { afterAll, beforeAll, beforeEach, chai, describe, expect, it } from 'vitest';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import createViewConfigJson from '../../src/vite-plugin/createViewConfigJson.js';
import { createTestingRouteFiles, createTestingRouteMeta, createTmpDir } from '../utils.js';

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
  });
});
