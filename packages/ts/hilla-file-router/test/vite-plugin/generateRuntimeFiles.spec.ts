import { existsSync, watch } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import { rimraf } from 'rimraf';
import sinon from 'sinon';
import type { Logger } from 'vite';
import { generateRuntimeFiles, type RuntimeFileUrls } from '../../src/vite-plugin/generateRuntimeFiles.js';
import { createTestingRouteFiles, createTmpDir } from '../utils.js';

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
        json: new URL('server/views.json', tmp),
        code: new URL('generated/views.ts', tmp),
      };

      await createTestingRouteFiles(viewsDir);
    });

    after(async () => {
      await rimraf(fileURLToPath(tmp));
    });

    beforeEach(() => {
      logger = {
        info: sinon.stub(),
        warn: sinon.stub(),
        warnOnce: sinon.stub(),
        error: sinon.stub(),
        clearScreen: sinon.stub(),
        hasErrorLogged: sinon.stub(),
        hasWarned: false,
      };
    });

    it('should generate the runtime files', async () => {
      await generateRuntimeFiles(viewsDir, runtimeUrls, ['.tsx', '.jsx', '.ts', '.js'], logger);
      expect(existsSync(runtimeUrls.json)).to.be.true;
      expect(existsSync(runtimeUrls.code)).to.be.true;
      const listener = () => {
        throw new Error('File is changed');
      };
      const json = watch(runtimeUrls.json, listener);
      const code = watch(runtimeUrls.code, listener);

      await generateRuntimeFiles(viewsDir, runtimeUrls, ['.tsx', '.jsx', '.ts', '.js'], logger);
      await new Promise((resolve) => {
        // Wait some time to ensure that the file is not changed
        setTimeout(resolve, 100);
      });
      json.close();
      code.close();
    });
  });
});
