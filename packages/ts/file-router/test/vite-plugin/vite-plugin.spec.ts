import { use } from '@esm-bundle/chai';
import chaiAsPromised from 'chai-as-promised';
// eslint-disable-next-line import/no-extraneous-dependencies
import { FSWatcher } from 'chokidar';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import vitePluginFileSystemRouter from '../../src/vite-plugin';

use(chaiAsPromised);
use(sinonChai);

describe('@vaadin/hilla-file-router', () => {
  describe('vite-plugin', () => {
    const watcher = new FSWatcher();
    const mockServer = {
      hot: {
        send: sinon.spy(),
      },
      watcher,
    };
    const plugin = vitePluginFileSystemRouter({ isDevMode: true });
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    plugin.configResolved({
      logger: { info: sinon.spy() },
      root: '/path/to/project',
      build: { outDir: '/path/to/project/dist' },
    });
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    plugin.configureServer(mockServer);

    beforeEach(() => {
      sinon.resetHistory();
    });

    it('should send full-reload only when file-routes.json is added', () => {
      sinon.assert.notCalled(mockServer.hot.send);
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      mockServer.watcher.emit('add', '/path/to/generated/file-routes.json');
      sinon.assert.calledWith(mockServer.hot.send, { type: 'full-reload' });
    });

    it('should send full-reload only when file-routes.json changes', () => {
      sinon.assert.notCalled(mockServer.hot.send);
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      mockServer.watcher.emit('change', '/path/to/generated/file-routes.json');
      sinon.assert.calledWith(mockServer.hot.send, { type: 'full-reload' });
    });

    it('should not send full-reload when other files change', () => {
      sinon.assert.notCalled(mockServer.hot.send);
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      mockServer.watcher.emit('change', '/path/to/views/file.tsx');
      sinon.assert.notCalled(mockServer.hot.send);
    });
  });
});
