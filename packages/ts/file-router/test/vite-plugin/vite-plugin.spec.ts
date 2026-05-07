/* eslint-disable @typescript-eslint/unbound-method */
import { existsSync, rmSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { runInNewContext } from 'node:vm';
import chaiAsPromised from 'chai-as-promised';
import type { TransformResult } from 'rollup';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { EnvironmentModuleNode, FetchModuleOptions, FetchResult, HotPayload, HotUpdateOptions } from 'vite';
import { afterAll, beforeAll, beforeEach, chai, describe, expect, it } from 'vitest';
import vitePluginFileSystemRouter from '../../src/vite-plugin';
import { createTestingRouteFiles, createTmpDir } from '../utils';

function runInstaller(installerCode: string, fakeWindow: object): void {
  runInNewContext(installerCode, { window: fakeWindow });
}

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

    describe('react-refresh ignore virtual module (dev mode)', () => {
      const VIRTUAL_ID = 'virtual:hilla-fs-router-refresh-ignore';
      const RESOLVED_VIRTUAL_ID = `\0${VIRTUAL_ID}`;

      function callTransform(code: string, id: string): TransformResult {
        return (plugin.transform as (this: unknown, code: string, id: string) => TransformResult).call({}, code, id);
      }

      function callResolveId(source: string): string | undefined {
        return (plugin.resolveId as (this: unknown, source: string) => string | undefined).call({}, source);
      }

      function callLoad(id: string): string | undefined {
        return (plugin.load as (this: unknown, id: string) => string | undefined).call({}, id);
      }

      it('prepends a side-effect import for view modules', () => {
        const viewFile = fileURLToPath(new URL('login.tsx', viewsDir)).replaceAll('\\', '/');
        const result = callTransform('export const config = {};\nexport default function Login() {}', viewFile);
        expect(result).to.be.an('object');
        expect((result as { code: string }).code).to.match(/^import "virtual:hilla-fs-router-refresh-ignore";\n/u);
      });

      it('does not transform underscored view files', () => {
        const ignoredFile = fileURLToPath(new URL('test/_ignored.tsx', viewsDir)).replaceAll('\\', '/');
        const result = callTransform('export default function Ignored() {}', ignoredFile);
        expect(result).to.equal(undefined);
      });

      it('does not transform files outside the views directory', () => {
        const outsideFile = `${fileURLToPath(generatedDir).replaceAll('\\', '/')}elsewhere.tsx`;
        const result = callTransform('export default function X() {}', outsideFile);
        expect(result).to.equal(undefined);
      });

      it('resolveId maps the virtual id to its resolved (\\0-prefixed) form', () => {
        expect(callResolveId(VIRTUAL_ID)).to.equal(RESOLVED_VIRTUAL_ID);
        expect(callResolveId('something-else')).to.equal(undefined);
      });

      it('load returns code that installs window.__getReactRefreshIgnoredExports', () => {
        const code = callLoad(RESOLVED_VIRTUAL_ID);
        expect(code).to.be.a('string');
        const viewsDirPosix = fileURLToPath(viewsDir).replaceAll('\\', '/');
        expect(code).to.include('window.__getReactRefreshIgnoredExports');
        expect(code).to.include("'config'");
        expect(code).to.include(JSON.stringify(viewsDirPosix));
      });

      it('load returns undefined for unrelated ids', () => {
        expect(callLoad('/some/other/id')).to.equal(undefined);
      });

      it('the installed hook returns ["config"] for view ids and inherits otherwise', () => {
        const viewsDirPosix = fileURLToPath(viewsDir).replaceAll('\\', '/');
        const fakeWindow: { __getReactRefreshIgnoredExports?(ctx: { id: string }): string[] } = {};
        runInstaller(callLoad(RESOLVED_VIRTUAL_ID)!, fakeWindow);

        const hook = fakeWindow.__getReactRefreshIgnoredExports!;
        expect(hook).to.be.a('function');
        expect(hook({ id: `${viewsDirPosix}login.tsx` })).to.deep.equal(['config']);
        expect(hook({ id: `${viewsDirPosix}login.tsx?t=12345` })).to.deep.equal(['config']);
        expect(hook({ id: '/elsewhere/file.tsx' })).to.deep.equal([]);
      });

      it('the installed hook chains with a pre-existing implementation', () => {
        const viewsDirPosix = fileURLToPath(viewsDir).replaceAll('\\', '/');
        const fakeWindow: {
          __getReactRefreshIgnoredExports?(ctx: { id: string }): string[];
        } = {
          __getReactRefreshIgnoredExports: () => ['preExisting'],
        };
        runInstaller(callLoad(RESOLVED_VIRTUAL_ID)!, fakeWindow);

        const hook = fakeWindow.__getReactRefreshIgnoredExports!;
        expect(hook({ id: `${viewsDirPosix}login.tsx` })).to.deep.equal(['preExisting', 'config']);
        expect(hook({ id: '/elsewhere/file.tsx' })).to.deep.equal(['preExisting']);
      });
    });

    describe('production-mode transform', () => {
      let prodPlugin: ReturnType<typeof vitePluginFileSystemRouter>;
      let prodViewsDir: URL;

      beforeAll(async () => {
        const prodRootDir = await createTmpDir();
        const prodOutDir = new URL('dist/', prodRootDir);
        prodViewsDir = new URL('frontend/views/', prodRootDir);
        await createTestingRouteFiles(prodViewsDir);
        prodPlugin = vitePluginFileSystemRouter({ isDevMode: false });
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error: the configResolved method could be either a function or an object.
        prodPlugin.configResolved({
          logger: { info: sinon.spy(), warn: sinon.spy(), error: sinon.spy() },
          root: fileURLToPath(prodRootDir),
          build: { outDir: fileURLToPath(prodOutDir) },
        });
      });

      function callTransform(code: string, id: string): TransformResult {
        return (prodPlugin.transform as (this: unknown, code: string, id: string) => TransformResult).call(
          {},
          code,
          id,
        );
      }

      it('does not prepend the virtual import in production mode', () => {
        const viewFile = fileURLToPath(new URL('login.tsx', prodViewsDir)).replaceAll('\\', '/');
        const result = callTransform('export default function Login() {}', viewFile);
        expect(result).to.be.an('object');
        expect((result as { code: string }).code).to.not.include('virtual:hilla-fs-router-refresh-ignore');
      });

      it('appends Object.defineProperty(name, ...) for named default exports', () => {
        const viewFile = fileURLToPath(new URL('login.tsx', prodViewsDir)).replaceAll('\\', '/');
        const result = callTransform('export default function Login() {}', viewFile);
        expect((result as { code: string }).code).to.include(
          'Object.defineProperty(Login, \'name\', { value: "Login" });',
        );
      });

      it('returns undefined when default export has no inferable name', () => {
        const viewFile = fileURLToPath(new URL('anonymous.tsx', prodViewsDir)).replaceAll('\\', '/');
        const result = callTransform('export default function () {}', viewFile);
        expect(result).to.equal(undefined);
      });
    });
  });
});
