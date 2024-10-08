import { EventEmitter } from 'node:events';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { expect, use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import vitePluginFileSystemRouter from '../../src/vite-plugin';

use(chaiAsPromised);
use(sinonChai);

describe('@vaadin/hilla-file-router', () => {
  describe('vite-plugin', () => {
    const rootDir = pathToFileURL('/path/to/project/');
    const outDir = new URL('dist/', rootDir);
    const viewsDir = new URL('frontend/views/', rootDir);
    const generatedDir = new URL('frontend/generated/', rootDir);
    const watcher = new EventEmitter();
    const mockServer = {
      hot: {
        send: sinon.spy(),
      },
      watcher,
    };
    const plugin = vitePluginFileSystemRouter({ isDevMode: true, debug: true });
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error: the configResolved method could be either a function or an object.
    plugin.configResolved({
      logger: { info: sinon.spy() },
      root: fileURLToPath(rootDir),
      build: { outDir: fileURLToPath(outDir) },
    });
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error: the configResolved method could be either a function or an object.
    plugin.configureServer(mockServer);

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
  });
});
