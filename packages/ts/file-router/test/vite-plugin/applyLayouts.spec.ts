import { writeFile } from 'node:fs/promises';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import chaiDeepEqualIgnoreUndefined from 'chai-deep-equal-ignore-undefined';
import applyLayouts from '../../src/vite-plugin/applyLayouts.js';
import type { RouteMeta } from '../../src/vite-plugin/collectRoutesFromFS.js';
import { createTmpDir } from '../utils.js';

use(chaiDeepEqualIgnoreUndefined);
use(chaiAsPromised);

describe('@vaadin/hilla-file-router', () => {
  describe('applyLayouts', () => {
    let tmp: URL;
    let layoutsFile: URL;

    before(async () => {
      tmp = await createTmpDir();
      layoutsFile = new URL('./layouts.json', tmp);
      await writeFile(layoutsFile, JSON.stringify([{ path: '/flow' }, { path: '/hilla/deep' }]));
    });

    it('should enable flow layout for matching routes', async () => {
      const meta: readonly RouteMeta[] = [
        { path: '' },
        {
          path: 'flow',
          children: [{ path: '' }, { path: 'hello-hilla' }],
        },
        {
          path: 'hilla',
          children: [
            { path: '' },
            { path: 'foo' },
            {
              path: 'deep',
              children: [{ path: 'flow' }],
            },
            {
              path: 'deepend',
              children: [{ path: 'no-layout' }],
            },
          ],
        },
        { path: 'login' },
      ];

      const result = await applyLayouts(meta, layoutsFile);

      expect(result).to.deepEqualIgnoreUndefined([
        { path: '' },
        {
          path: 'flow',
          flowLayout: true,
          children: [
            { path: '', flowLayout: true },
            { path: 'hello-hilla', flowLayout: true },
          ],
        },
        {
          path: 'hilla',
          children: [
            { path: '' },
            { path: 'foo' },
            {
              path: 'deep',
              flowLayout: true,
              children: [
                {
                  path: 'flow',
                  flowLayout: true,
                },
              ],
            },
            {
              path: 'deepend',
              children: [{ path: 'no-layout' }],
            },
          ],
        },
        { path: 'login' },
      ]);
    });

    it('should return original route metas if the file is not found', async () => {
      await expect(applyLayouts([{ path: '/flow' }], new URL('./non-existing.json', tmp))).to.eventually.be.deep.equal([
        { path: '/flow' },
      ]);
    });

    it('should throw an error in an unexpected case', async () => {
      await writeFile(layoutsFile, '[{path: "/flow');
      await expect(applyLayouts([{ path: '/flow' }], layoutsFile)).to.be.rejectedWith(Error);
    });
  });
});
