import { EventEmitter } from 'node:events';
import { existsSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import vitePluginFileSystemRouter from '../../src/vite-plugin';
import { createTestingRouteFiles, createTmpDir } from '../utils';

use(chaiAsPromised);
use(sinonChai);

describe('@vaadin/hilla-file-router', () => {
  describe('vite-plugin', () => {
    let mockServer: { hot: { send: any }; watcher: EventEmitter };
    let generatedDir: URL;
    let viewsDir: URL;

    async function expectRuntimeFileGenerated(watcherEvent: string) {
      await createTestingRouteFiles(viewsDir);
      mockServer.watcher.emit(watcherEvent, fileURLToPath(new URL('layouts.json', generatedDir)));
      await new Promise((resolve) => {
        // Wait some time to ensure that the files have been written
        setTimeout(resolve, 1000);
      });
      expect(existsSync(new URL('file-routes.json', generatedDir))).to.be.true;
      expect(mockServer.hot.send).to.not.be.called;
    }

    before(async () => {
      // const rootDir = pathToFileURL('/path/to/project/');
      const rootDir = await createTmpDir();
      const outDir = new URL('dist/', rootDir);
      viewsDir = new URL('frontend/views/', rootDir);
      generatedDir = new URL('frontend/generated/', rootDir);
      const watcher = new EventEmitter();
      mockServer = {
        hot: {
          send: sinon.spy(),
        },
        watcher,
      };
      const plugin = vitePluginFileSystemRouter({ isDevMode: true, debug: true });
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error: the configResolved method could be either a function or an object.
      plugin.configResolved({
        logger: { info: sinon.spy(), warn: sinon.spy(), error: sinon.spy() },
        root: fileURLToPath(rootDir),
        build: { outDir: fileURLToPath(outDir) },
      });
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error: the configResolved method could be either a function or an object.
      plugin.configureServer(mockServer);
    });

    beforeEach(() => {
      sinon.resetHistory();
    });

    it('should send fs-route-update when file-routes.json is added', () => {
      expect(mockServer.hot.send).to.not.be.called;
      mockServer.watcher.emit('add', fileURLToPath(new URL('file-routes.json', generatedDir)));
      expect(mockServer.hot.send).to.be.calledWith({ type: 'custom', event: 'fs-route-update' });
    });

    it('should send fs-route-update when file-routes.json changes', () => {
      expect(mockServer.hot.send).to.not.be.called;
      mockServer.watcher.emit('change', fileURLToPath(new URL('file-routes.json', generatedDir)));
      expect(mockServer.hot.send).to.be.calledWith({ type: 'custom', event: 'fs-route-update' });
    });

    it('should not send full-reload when other files change', () => {
      expect(mockServer.hot.send).to.not.be.called;
      mockServer.watcher.emit('change', fileURLToPath(new URL('file.tsx', viewsDir)));
      expect(mockServer.hot.send).to.not.be.called;
    });

    it('should regenerate files when layouts.json is updated', async () => {
      await expectRuntimeFileGenerated('change');
    });

    it('should regenerate files when layouts.json is added', async () => {
      await expectRuntimeFileGenerated('add');
    });
  });
});
