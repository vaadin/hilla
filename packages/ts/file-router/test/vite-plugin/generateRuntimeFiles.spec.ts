import { existsSync, watch } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import { rimraf } from 'rimraf';
import type { Logger } from 'vite';
import { generateRuntimeFiles, type RuntimeFileUrls } from '../../src/vite-plugin/generateRuntimeFiles.js';
import { createLogger, createTestingRouteFiles, createTmpDir } from '../utils.js';

use(chaiAsPromised);

describe('@vaadin/hilla-file-router', () => {
  describe('generateRuntimeFiles', () => {
    let tmp: URL;
    let viewsDir: URL;
    let runtimeUrls: RuntimeFileUrls;
    let logger: Logger;

    before(async () => {
      tmp = await createTmpDir();

      viewsDir = new URL('views/', tmp);
      runtimeUrls = {
        json: new URL('server/file-routes.json', tmp),
        code: new URL('generated/file-routes.ts', tmp),
      };

      await createTestingRouteFiles(viewsDir);
    });

    after(async () => {
      await rimraf(fileURLToPath(tmp));
    });

    beforeEach(() => {
      logger = createLogger();
    });

    it('should generate the runtime files', async () => {
      await generateRuntimeFiles(viewsDir, runtimeUrls, ['.tsx', '.jsx'], logger);
      expect(existsSync(runtimeUrls.json)).to.be.true;
      expect(existsSync(runtimeUrls.code)).to.be.true;
      const listener = () => {
        throw new Error('File is changed');
      };
      const json = watch(runtimeUrls.json, listener);
      const code = watch(runtimeUrls.code, listener);

      await generateRuntimeFiles(viewsDir, runtimeUrls, ['.tsx', '.jsx'], logger);
      await new Promise((resolve) => {
        // Wait some time to ensure that the file is not changed
        setTimeout(resolve, 100);
      });
      json.close();
      code.close();
    });
  });
});
