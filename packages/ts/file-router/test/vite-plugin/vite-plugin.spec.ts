/* eslint-disable @typescript-eslint/unbound-method */
import { existsSync, rmSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { runInNewContext } from 'node:vm';
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

    describe('transform', () => {
      const VIRTUAL_ID = 'virtual:hilla-file-router-refresh-ignore';
      const RESOLVED_VIRTUAL_ID = `\0${VIRTUAL_ID}`;

      type TransformFn = (code: string, id: string) => { code: string } | undefined;

      function transform(code: string, id: string): { code: string } | undefined {
        return (plugin.transform as TransformFn)(code, id);
      }

      const viewFileCode = `import { ReactElement } from 'react';
export const config = { menu: { exclude: true } };
export default function MyView(): ReactElement { return <div></div>; }
`;

      it('should inject React Refresh ignored exports registration to view files in dev mode', () => {
        const id = fileURLToPath(new URL('file.tsx', viewsDir)).replaceAll('\\', '/');
        const result = transform(viewFileCode, id);
        expect(result).to.exist;
        expect(result!.code).to.contain(viewFileCode);
        expect(result!.code).to.contain(`import ${JSON.stringify(VIRTUAL_ID)};`);
        expect(result!.code).to.contain(`.add(${JSON.stringify(id)});`);
      });

      it('should not inject to private view files', () => {
        const id = fileURLToPath(new URL('_private.tsx', viewsDir)).replaceAll('\\', '/');
        const result = transform(viewFileCode, id);
        expect(result).to.equal(undefined);
      });

      it('should not inject to files outside the views directory', () => {
        const id = fileURLToPath(new URL('../other/file.tsx', viewsDir)).replaceAll('\\', '/');
        const result = transform(viewFileCode, id);
        expect(result).to.equal(undefined);
      });

      it('should not inject to view directory files with other extensions', () => {
        const id = fileURLToPath(new URL('styles.css', viewsDir)).replaceAll('\\', '/');
        const result = transform('.my-view { color: red; }', id);
        expect(result!.code).to.not.contain(VIRTUAL_ID);
      });

      describe('refresh ignore virtual module', () => {
        function resolveId(source: string): string | undefined {
          return (plugin.resolveId as (source: string) => string | undefined)(source);
        }

        function load(id: string): string | undefined {
          return (plugin.load as (id: string) => string | undefined)(id);
        }

        it('should map the virtual module id to its resolved form', () => {
          expect(resolveId(VIRTUAL_ID)).to.equal(RESOLVED_VIRTUAL_ID);
          expect(resolveId('something-else')).to.equal(undefined);
        });

        it('should load the React Refresh hook installer for the resolved virtual module id', () => {
          const code = load(RESOLVED_VIRTUAL_ID);
          expect(code).to.be.a('string');
          expect(code).to.contain('window.__getReactRefreshIgnoredExports');
          expect(code).to.contain("'config'");
          expect(load('/some/other/id')).to.equal(undefined);
        });

        describe('installed hook', () => {
          type RefreshIgnoredExportsHook = (ctx: { id: string }) => readonly string[];
          type FakeWindow = {
            __HILLA_FILE_ROUTER_VIEWS__?: Set<string>;
            __getReactRefreshIgnoredExports?: RefreshIgnoredExportsHook;
          };

          function installHook(fakeWindow: FakeWindow): RefreshIgnoredExportsHook {
            runInNewContext(load(RESOLVED_VIRTUAL_ID)!, { window: fakeWindow });
            return fakeWindow.__getReactRefreshIgnoredExports!;
          }

          it('should ignore the config export of registered view modules only', () => {
            const id = fileURLToPath(new URL('file.tsx', viewsDir)).replaceAll('\\', '/');
            const hook = installHook({ __HILLA_FILE_ROUTER_VIEWS__: new Set([id]) });
            expect(hook({ id })).to.deep.equal(['config']);
            expect(hook({ id: '/elsewhere/file.tsx' })).to.deep.equal([]);
          });

          it('should return no ignored exports before any view module is registered', () => {
            const hook = installHook({});
            expect(hook({ id: '/any/file.tsx' })).to.deep.equal([]);
          });

          it('should chain with a pre-existing hook', () => {
            const id = fileURLToPath(new URL('file.tsx', viewsDir)).replaceAll('\\', '/');
            const hook = installHook({
              __HILLA_FILE_ROUTER_VIEWS__: new Set([id]),
              __getReactRefreshIgnoredExports: () => ['preExisting'],
            });
            expect(hook({ id })).to.deep.equal(['preExisting', 'config']);
            expect(hook({ id: '/elsewhere/file.tsx' })).to.deep.equal(['preExisting']);
          });
        });
      });

      describe('production mode', () => {
        let prodPlugin: ReturnType<typeof vitePluginFileSystemRouter>;
        let prodViewsDir: URL;

        beforeAll(async () => {
          const prodRootDir = await createTmpDir();
          prodViewsDir = new URL('frontend/views/', prodRootDir);
          prodPlugin = vitePluginFileSystemRouter({ isDevMode: false });
          // eslint-disable-next-line @typescript-eslint/ban-ts-comment
          // @ts-expect-error: the configResolved method could be either a function or an object.
          prodPlugin.configResolved({
            logger: { info: sinon.spy(), warn: sinon.spy(), error: sinon.spy() },
            root: fileURLToPath(prodRootDir),
            build: { outDir: fileURLToPath(new URL('dist/', prodRootDir)) },
          });
        });

        function prodTransform(code: string, id: string): { code: string } | undefined {
          return (prodPlugin.transform as TransformFn)(code, id);
        }

        it('should not inject React Refresh ignored exports registration', () => {
          const id = fileURLToPath(new URL('file.tsx', prodViewsDir)).replaceAll('\\', '/');
          const result = prodTransform('export default function MyView() {}', id);
          expect(result!.code).to.not.contain(VIRTUAL_ID);
        });

        it('should preserve the name of a named default export', () => {
          const id = fileURLToPath(new URL('file.tsx', prodViewsDir)).replaceAll('\\', '/');
          const result = prodTransform('export default function MyView() {}', id);
          expect(result!.code).to.contain("Object.defineProperty(MyView, 'name', { value: 'MyView' });");
        });

        it('should preserve the name of a named default class export', () => {
          const id = fileURLToPath(new URL('file.tsx', prodViewsDir)).replaceAll('\\', '/');
          const result = prodTransform('export default class MyView extends Component {}', id);
          expect(result!.code).to.contain("Object.defineProperty(MyView, 'name', { value: 'MyView' });");
        });

        it('should not modify anonymous default exports', () => {
          const id = fileURLToPath(new URL('file.tsx', prodViewsDir)).replaceAll('\\', '/');
          const code = 'export default function () {}';
          const result = prodTransform(code, id);
          expect(result!.code).to.equal(code);
        });
      });
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
