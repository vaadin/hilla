/* eslint-disable @typescript-eslint/unbound-method */
import { existsSync, rmSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { EnvironmentModuleNode, FetchModuleOptions, FetchResult, HotPayload, HotUpdateOptions } from 'vite';
import { afterAll, beforeAll, beforeEach, chai, describe, expect, it } from 'vitest';
import vitePluginFileSystemRouter from '../../src/vite-plugin';
import { createTestingRouteFiles, createTmpDir } from '../utils';

chai.use(chaiAsPromised);
chai.use(sinonChai);

describe('@vaadin/hilla-file-router', () => {
  describe('vite-plugin', () => {
    let mockModuleGraph: {
      getModulesByFile(file: string): Set<EnvironmentModuleNode>;
      invalidateModule(module: EnvironmentModuleNode): void;
    };
    let mockEnvironment: {
      hot: {
        send(payload: HotPayload): void;
      };
      fetchModule(id: string, importer?: string, options?: FetchModuleOptions): Promise<FetchResult>;
      moduleGraph: typeof mockModuleGraph;
    };
    let generatedDir: URL;
    let viewsDir: URL;
    let plugin: ReturnType<typeof vitePluginFileSystemRouter>;
    let viewModule: EnvironmentModuleNode;
    let fileRoutesTsModule: EnvironmentModuleNode;
    let fileRoutesJsonModule: EnvironmentModuleNode;

    function createMockEnvironmentModuleNode(fileName: string, dir: URL): EnvironmentModuleNode {
      const file = fileURLToPath(new URL(fileName, dir)).replaceAll('\\', '/');
      return {
        id: file,
        file,
        importers: new Set(),
      } as unknown as EnvironmentModuleNode;
    }

    async function hotUpdate({
      type,
      file,
      modules,
    }: Partial<HotUpdateOptions>): Promise<EnvironmentModuleNode[] | void> {
      return await (
        plugin.hotUpdate as (
          this: unknown,
          options: Partial<HotUpdateOptions>,
        ) => Promise<EnvironmentModuleNode[] | void>
      ).call(
        { environment: mockEnvironment },
        {
          type,
          file,
          timestamp: 0,
          modules,
        },
      );
    }

    beforeAll(async () => {
      // const rootDir = pathToFileURL('/path/to/project/');
      const rootDir = await createTmpDir();
      const outDir = new URL('dist/', rootDir);
      viewsDir = new URL('frontend/views/', rootDir);
      await createTestingRouteFiles(viewsDir);
      generatedDir = new URL('frontend/generated/', rootDir);
      viewModule = createMockEnvironmentModuleNode('file.tsx', viewsDir);
      fileRoutesTsModule = createMockEnvironmentModuleNode('file-routes.ts', generatedDir);
      fileRoutesJsonModule = createMockEnvironmentModuleNode('file-routes.json', generatedDir);
      mockModuleGraph = {
        getModulesByFile(file: string) {
          if (file === fileRoutesTsModule.file) {
            return new Set([fileRoutesTsModule]);
          }
          return new Set();
        },
        invalidateModule: sinon.spy(),
      };
      mockEnvironment = {
        hot: {
          send: sinon.spy(),
        },
        fetchModule: sinon.spy(async (id: string) =>
          Promise.resolve({
            id,
            file: id,
          } as FetchResult),
        ),
        moduleGraph: mockModuleGraph,
      };
      plugin = vitePluginFileSystemRouter({ isDevMode: true, debug: true });
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error: the configResolved method could be either a function or an object.
      plugin.configResolved({
        logger: { info: sinon.spy(), warn: sinon.spy(), error: sinon.spy() },
        root: fileURLToPath(rootDir),
        build: { outDir: fileURLToPath(outDir) },
      });
    });

    beforeEach(() => {
      if (existsSync(fileRoutesJsonModule.file!)) {
        rmSync(fileRoutesJsonModule.file!);
      }
      sinon.resetHistory();
    });

    it('should generate fs routes during build', async () => {
      await (plugin.buildStart as () => Promise<void>)();
      expect(existsSync(fileRoutesJsonModule.file!)).to.be.true;
    });

    it('should send fs-route-update when file-routes.json is added', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const modulesToUpdate = await hotUpdate({ type: 'create', file: fileRoutesJsonModule.file! });
      expect(modulesToUpdate).to.be.an('array').that.is.empty;
      expect(mockEnvironment.hot.send).to.be.calledWith({ type: 'custom', event: 'fs-route-update' });
    });

    it('should send fs-route-update when file-routes.json changes', async () => {
      const modulesToUpdate = await hotUpdate({ type: 'update', file: fileRoutesJsonModule.file! });
      expect(modulesToUpdate).to.be.an('array').that.is.empty;
      expect(mockEnvironment.hot.send).to.be.calledWith({ type: 'custom', event: 'fs-route-update' });
    });

    it('should not send updates for file-routes.ts separately', async () => {
      const modulesToUpdate = await hotUpdate({ type: 'update', file: fileRoutesTsModule.file! });
      // Updates to file-routes.ts are not processed separately, instead they
      // are combined with the update of the triggering file.
      expect(modulesToUpdate).to.be.an('array').that.is.empty;
      expect(mockEnvironment.hot.send).to.not.be.called;
    });

    describe('view module imported in file routes', () => {
      beforeAll(() => {
        viewModule.importers.add(viewModule);
      });

      afterAll(() => {
        viewModule.importers.clear();
      });

      it('should send hot reload including file routes when a view file changes', async () => {
        const modulesToUpdate = await hotUpdate({
          type: 'update',
          file: viewModule.file!,
          modules: [viewModule],
        });
        expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsModule.id!);
        // HMR for routes and view file together.
        expect(modulesToUpdate).to.deep.equal([fileRoutesTsModule, viewModule]);
        expect(mockEnvironment.hot.send).to.not.be.called;
      });

      it('should send hot reload including file routes when a view file is added', async () => {
        const modulesToUpdate = await hotUpdate({
          type: 'create',
          file: viewModule.file!,
          modules: [viewModule],
        });
        expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsModule.id!);
        // HMR for routes and view file together.
        expect(modulesToUpdate).to.deep.equal([fileRoutesTsModule, viewModule]);
        expect(mockEnvironment.hot.send).to.not.be.called;
      });

      it('should send regular updates when a view file but not routes change', async () => {
        // Trigger update for "file-routes.ts".
        await hotUpdate({ type: 'update', file: viewModule.file!, modules: [viewModule] });
        sinon.resetHistory();
        // Next update is expected to skip file routes as they did not change.
        const modulesToUpdate = await hotUpdate({
          type: 'update',
          file: viewModule.file!,
          modules: [viewModule],
        });
        expect(mockEnvironment.fetchModule).to.not.be.called;
        // undefined result makes Vite proceed with regular HMR.
        expect(modulesToUpdate).to.equal(undefined);
      });
    });

    describe('view module not imported anywhere', () => {
      it('should send hot reload for only file routes when a view file changes', async () => {
        const modulesToUpdate = await hotUpdate({
          type: 'update',
          file: viewModule.file!,
          modules: [viewModule],
        });
        expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsModule.id!);
        // HMR for only file routes.
        expect(modulesToUpdate).to.be.deep.equal([fileRoutesTsModule]);
        expect(mockEnvironment.hot.send).to.not.be.called;
      });

      it('should send hot reload for only file routes when a view file is removed', async () => {
        const modulesToUpdate = await hotUpdate({
          type: 'delete',
          file: viewModule.file!,
          modules: [viewModule],
        });
        expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsModule.id!);
        // HMR for only file routes.
        expect(modulesToUpdate).to.deep.equal([fileRoutesTsModule]);
        expect(mockEnvironment.hot.send).to.not.be.called;
      });
    });

    it('should regenerate files when layouts.json is updated', async () => {
      const layoutJsonModule = createMockEnvironmentModuleNode('layouts.json', generatedDir);
      layoutJsonModule.importers.add(fileRoutesTsModule);
      const modulesToUpdate = await hotUpdate({
        type: 'update',
        file: layoutJsonModule.file!,
        modules: [layoutJsonModule],
      });
      expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsModule.id!);
      expect(modulesToUpdate).to.be.deep.equal([fileRoutesTsModule, layoutJsonModule]);
      expect(existsSync(fileRoutesJsonModule.file!)).to.be.true;
    });

    it('should not regenerate files when another file is updated', async () => {
      const someJsonModule = createMockEnvironmentModuleNode('something.json', generatedDir);
      someJsonModule.importers.add(fileRoutesTsModule);
      const modulesToUpdate = await hotUpdate({
        type: 'update',
        file: someJsonModule.file!,
        modules: [someJsonModule],
      });
      expect(mockEnvironment.fetchModule).to.not.be.called;
      expect(modulesToUpdate).to.equal(undefined);
      expect(existsSync(fileRoutesJsonModule.file!)).to.be.false;
    });
  });
});
