/* eslint-disable @typescript-eslint/unbound-method */
import { existsSync, rmSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { EnvironmentModuleNode, FetchModuleOptions, FetchResult, HotPayload, HotUpdateOptions } from 'vite';
import { beforeAll, beforeEach, chai, describe, expect, it } from 'vitest';
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
    let fileRoutesTsPath: string;
    let fileRoutesJsonPath: string;
    let fileRoutesModule: EnvironmentModuleNode;

    function createMockEnvironmentModuleNode(file: string): EnvironmentModuleNode {
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

    async function expectRuntimeFileGeneratedWithHotUpdate(
      changedFileName: string,
      expectGeneration: boolean,
      importer?: string,
    ): Promise<void> {
      const file = fileURLToPath(new URL(changedFileName, generatedDir));
      await createTestingRouteFiles(viewsDir);
      const fileModule = createMockEnvironmentModuleNode(file);
      if (importer) {
        fileModule.importers.add(createMockEnvironmentModuleNode(importer));
      }
      const modules = [fileModule];
      const modulesToUpdate = await hotUpdate({ type: 'update', file, modules });
      if (expectGeneration) {
        expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsPath);
        if (importer) {
          expect(modulesToUpdate).to.be.deep.equal([fileRoutesModule, fileModule]);
        } else {
          // No importer — only file routes HMR should happen to avoid full
          // page reload
          expect(modulesToUpdate).to.be.deep.equal([fileRoutesModule]);
        }
      } else {
        expect(mockEnvironment.fetchModule).to.not.be.called;
        expect(modulesToUpdate).to.equal(undefined);
      }
      expect(existsSync(fileRoutesJsonPath)).to.equal(expectGeneration);
    }

    beforeAll(async () => {
      // const rootDir = pathToFileURL('/path/to/project/');
      const rootDir = await createTmpDir();
      const outDir = new URL('dist/', rootDir);
      viewsDir = new URL('frontend/views/', rootDir);
      generatedDir = new URL('frontend/generated/', rootDir);
      fileRoutesTsPath = fileURLToPath(new URL('file-routes.ts', generatedDir));
      fileRoutesJsonPath = fileURLToPath(new URL('file-routes.json', generatedDir));
      fileRoutesModule = createMockEnvironmentModuleNode(fileRoutesTsPath);
      mockModuleGraph = {
        getModulesByFile(file: string) {
          if (file === fileRoutesTsPath) {
            return new Set([fileRoutesModule]);
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
      if (existsSync(fileRoutesJsonPath)) {
        rmSync(fileRoutesJsonPath);
      }
      sinon.resetHistory();
    });

    it('should send fs-route-update when file-routes.json is added', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const modulesToUpdate = await hotUpdate({ type: 'create', file: fileRoutesJsonPath });
      expect(mockEnvironment.hot.send).to.be.calledWith({ type: 'custom', event: 'fs-route-update' });
      expect(modulesToUpdate).to.be.deep.equal([]);
    });

    it('should send fs-route-update when file-routes.json changes', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const modulesToUpdate = await hotUpdate({ type: 'update', file: fileRoutesJsonPath });
      expect(mockEnvironment.hot.send).to.be.calledWith({ type: 'custom', event: 'fs-route-update' });
      expect(modulesToUpdate).to.be.deep.equal([]);
    });

    it('should not send updates for file-routes.ts separately', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const modulesToUpdate = await hotUpdate({ type: 'update', file: fileRoutesTsPath });
      // Updates to file-routes.ts are not processed separately, instead they
      // are combined with the update of the triggering file.
      expect(mockEnvironment.hot.send).to.not.be.called;
      expect(modulesToUpdate).to.be.deep.equal([]);
    });

    it('should send hot reload including file routes when a view file imported in file routes changes', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const viewFilePath = fileURLToPath(new URL('file.tsx', viewsDir));
      const viewFileModule = createMockEnvironmentModuleNode(viewFilePath);
      viewFileModule.importers.add(fileRoutesModule);
      const modulesToUpdate = await hotUpdate({ type: 'update', file: viewFilePath, modules: [viewFileModule] });
      expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsPath);
      // HMR for routes and view file together.
      expect(modulesToUpdate).to.not.equal(undefined);
      expect(modulesToUpdate!.length).to.equal(2);
      expect(modulesToUpdate![0]).to.eq(fileRoutesModule);
      expect(modulesToUpdate![1]).to.eq(viewFileModule);
      expect(mockEnvironment.hot.send).to.not.be.called;
    });

    it('should send hot reload including file routes when a view file imported in file routes is added', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const viewFilePath = fileURLToPath(new URL('file.tsx', viewsDir));
      const viewFileModule = createMockEnvironmentModuleNode(viewFilePath);
      viewFileModule.importers.add(fileRoutesModule);
      const modulesToUpdate = await hotUpdate({ type: 'create', file: viewFilePath, modules: [viewFileModule] });
      expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsPath);
      // HMR for routes and view file together.
      expect(modulesToUpdate).to.not.equal(undefined);
      expect(modulesToUpdate!.length).to.equal(2);
      expect(modulesToUpdate![0]).to.eq(fileRoutesModule);
      expect(modulesToUpdate![1]).to.eq(viewFileModule);
      expect(mockEnvironment.hot.send).to.not.be.called;
    });

    it('should send hot reload for only file routes when a view file not imported anywhere changes', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const viewFilePath = fileURLToPath(new URL('file.tsx', viewsDir));
      const viewFileModule = createMockEnvironmentModuleNode(viewFilePath);
      const modulesToUpdate = await hotUpdate({ type: 'update', file: viewFilePath, modules: [viewFileModule] });
      expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsPath);
      // HMR for only file routes.
      expect(modulesToUpdate).to.not.equal(undefined);
      expect(modulesToUpdate!.length).to.equal(1);
      expect(modulesToUpdate![0]).to.eq(fileRoutesModule);
      expect(mockEnvironment.hot.send).to.not.be.called;
    });

    it('should send hot reload for only file routes when a view file not imported anywhere is removed', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const viewFilePath = fileURLToPath(new URL('file.tsx', viewsDir));
      const viewFileModule = createMockEnvironmentModuleNode(viewFilePath);
      const modulesToUpdate = await hotUpdate({ type: 'delete', file: viewFilePath, modules: [viewFileModule] });
      expect(mockEnvironment.fetchModule).to.be.calledWith(fileRoutesTsPath);
      // HMR for only file routes.
      expect(modulesToUpdate).to.not.equal(undefined);
      expect(modulesToUpdate!.length).to.equal(1);
      expect(modulesToUpdate![0]).to.eq(fileRoutesModule);
      expect(mockEnvironment.hot.send).to.not.be.called;
    });

    it('should send regular updates when a view file but not routes change', async () => {
      expect(mockEnvironment.hot.send).to.not.be.called;
      const viewFilePath = fileURLToPath(new URL('file.tsx', viewsDir));
      const viewFileModule = createMockEnvironmentModuleNode(viewFilePath);
      viewFileModule.importers.add(fileRoutesModule);
      // Trigger update for "file-routes.ts".
      await hotUpdate({ type: 'update', file: viewFilePath, modules: [viewFileModule] });
      sinon.resetHistory();
      // Next update is expected to skip file routes as they did not change.
      const modulesToUpdate = await hotUpdate({ type: 'update', file: viewFilePath, modules: [viewFileModule] });
      expect(mockEnvironment.fetchModule).to.not.be.called;
      // undefined result makes Vite proceed with regular HMR.
      expect(modulesToUpdate).to.equal(undefined);
    });

    it('should regenerate files when layouts.json is updated', async () => {
      await expectRuntimeFileGeneratedWithHotUpdate('layouts.json', true);
    });

    it('should not regenerate files when another file is updated', async () => {
      await expectRuntimeFileGeneratedWithHotUpdate('something.json', false);
    });
  });
});
