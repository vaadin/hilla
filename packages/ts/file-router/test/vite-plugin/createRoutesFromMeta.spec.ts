import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { beforeEach, describe, expect, it } from 'vitest';
import type { ServerViewConfig } from '../../src/shared/internal.js';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import createRoutesFromMeta from '../../src/vite-plugin/createRoutesFromMeta.js';
import type { RuntimeFileUrls } from '../../src/vite-plugin/generateRuntimeFiles.js';
import { createTestingRouteMeta, createTestingServerViewConfigs } from '../utils.js';

describe('@vaadin/hilla-file-router', () => {
  describe('generateRoutes', () => {
    let dir: URL;
    let meta: readonly RouteMeta[];
    let configs: readonly ServerViewConfig[];
    let runtimeUrls: RuntimeFileUrls;

    beforeEach(() => {
      dir = pathToFileURL(join(tmpdir(), 'file-router/'));
      meta = createTestingRouteMeta(new URL('./views/', dir));
      configs = createTestingServerViewConfigs(meta);
      runtimeUrls = {
        json: new URL('server/file-routes.json', dir),
        code: new URL('generated/file-routes.ts', dir),
        layouts: new URL('generated/layouts.json', dir),
      };
    });

    it('should generate a framework-agnostic tree of routes', async () => {
      meta = [
        ...meta,
        {
          path: 'issue-2928-flow-auto-layout',
          file: new URL('issue-2928-flow-auto-layout.tsx', dir),
          flowLayout: true,
        },
        {
          path: 'mod-extension-only',
          flowLayout: true,
          children: [
            {
              path: 'mod-extension-only-child',
              file: new URL('mod-extension-only-child.tsx', dir),
            },
          ],
        },
      ];

      configs = createTestingServerViewConfigs(meta);

      const generated = createRoutesFromMeta(meta, configs, runtimeUrls);

      await expect(generated).toMatchFileSnapshot('fixtures/createRoutesFromMeta/basic.snap.ts');
    });

    it('should generate an empty list when no routes are found', async () => {
      const generated = createRoutesFromMeta([], [], runtimeUrls);

      await expect(generated).toMatchFileSnapshot('fixtures/createRoutesFromMeta/empty-list.snap.ts');
    });

    it('should add console.error calls for duplicated paths', async () => {
      const metaWithDuplicatedPaths = [
        {
          ...meta[0]!,
          children: [
            ...(meta[0]!.children ?? []),
            {
              path: 'profile',
              file: new URL('profile/@index.tsx', dir),
            },
          ],
        },
      ];
      configs = createTestingServerViewConfigs(metaWithDuplicatedPaths);
      const generated = createRoutesFromMeta(metaWithDuplicatedPaths, configs, runtimeUrls);
      await expect(generated).toMatchFileSnapshot('fixtures/createRoutesFromMeta/duplicated-paths.snap.ts');
    });
  });
});
