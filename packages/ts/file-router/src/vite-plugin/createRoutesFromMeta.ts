import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ast from 'tsc-template';
import {
  type CallExpression,
  createPrinter,
  factory,
  type Identifier,
  NewLineKind,
  type ObjectLiteralExpression,
  type PropertyAccessExpression,
} from 'typescript';
import type { ServerViewConfig } from '../shared/internal.js';
import { transformTree } from '../shared/transformTree.js';
import type { RouteMeta } from './collectRoutesFromFS.js';
import type { RuntimeFileUrls } from './generateRuntimeFiles.js';
import { convertFSRouteSegmentToURLPatternFormat } from './utils.js';

const printer = createPrinter({ newLine: NewLineKind.LineFeed });

const fileExtensions = ['.ts', '.tsx', '.js', '.jsx'];

const HILLA_FILE_ROUTER = '@vaadin/hilla-file-router';
const HILLA_FILE_ROUTER_RUNTIME = `${HILLA_FILE_ROUTER}/runtime.js`;
const HILLA_FILE_ROUTER_TYPES = `${HILLA_FILE_ROUTER}/types.js`;

type LazyModule = Readonly<{
  path: string;
  config: ServerViewConfig;
  lazyId: Identifier;
}>;

type RegularModule = Readonly<{
  componentId: PropertyAccessExpression;
  configId: PropertyAccessExpression;
  flowLayout?: boolean;
}>;

function isLazyModule(mod: LazyModule | RegularModule | undefined): mod is LazyModule {
  return !!mod && 'path' in mod;
}

function isLazyRoute(pathContext: readonly string[], path: string, config: Readonly<ServerViewConfig>): boolean {
  if (config.lazy !== undefined) {
    return config.lazy;
  }

  // "/" and "/login" are eager by default
  const rootContext = !pathContext.some(Boolean);
  const eagerDefault = rootContext && (path === '' || path === 'login');
  return !eagerDefault;
}

class RouteFromMetaProcessor {
  readonly #manager: DependencyManager;
  readonly #views: readonly RouteMeta[];
  readonly #configs: readonly ServerViewConfig[];

  constructor(
    views: readonly RouteMeta[],
    viewConfigs: readonly ServerViewConfig[],
    { code: codeFile }: RuntimeFileUrls,
  ) {
    this.#views = views;
    this.#configs = viewConfigs;

    const codeDir = new URL('./', codeFile);
    this.#manager = new DependencyManager(new PathManager({ extension: '.js', relativeTo: codeDir }));
  }

  /**
   * Loads all the files from the received metadata and creates a framework-agnostic route tree.
   */
  process(): string {
    const {
      paths,
      imports: { namespace, named },
    } = this.#manager;
    const errors: string[] = [];

    const routes = transformTree<
      [pathContext: readonly string[], metas: readonly RouteMeta[], configs: readonly ServerViewConfig[]],
      readonly CallExpression[]
    >([[], this.#views, this.#configs], null, ([pathContext, metas, configs], next) => {
      errors.push(
        ...metas
          .map((route) => route.path)
          .filter((item, index, arr) => arr.indexOf(item) !== index)
          .map((dup) => `console.error("Two views share the same path: ${dup}");`),
      );

      return metas.map(({ file, layout, path, children, flowLayout }, index) => {
        const config = configs[index]!;
        let _children: readonly CallExpression[] | undefined;

        if (children) {
          // Assuming that if we have `children` in the route, we also have
          // `children` in the config.
          _children = next([[...pathContext, path], children, config.children!]);
        }

        let module: LazyModule | RegularModule | undefined;

        let relativePath: string | undefined;
        let fileName: string | undefined;

        if (file) {
          const fileExt = fileExtensions.find((ext) => file.pathname.endsWith(ext));
          relativePath = paths.createRelativePath(file, fileExt);
          fileName = 'Page';
        } else if (layout) {
          const fileExt = fileExtensions.find((ext) => layout.pathname.endsWith(ext));
          relativePath = paths.createRelativePath(layout, fileExt);
          fileName = 'Layout';
        }

        if (relativePath && fileName) {
          if (isLazyRoute(pathContext, path, config)) {
            module = {
              path: relativePath,
              config,
              lazyId: named.getIdentifier('react', 'lazy') ?? named.add('react', 'lazy'),
            };
          } else {
            const mod = namespace.add(relativePath, fileName);
            const reactModuleType =
              named.getIdentifier(HILLA_FILE_ROUTER_TYPES, 'RouteModule') ??
              named.add(HILLA_FILE_ROUTER_TYPES, 'RouteModule', true);

            module = {
              componentId: ast`${mod}.default`.node as PropertyAccessExpression,
              configId: ast`(${mod} as ${reactModuleType}).config`.node as PropertyAccessExpression,
              flowLayout,
            };
          }
        }

        return this.#createRouteData(convertFSRouteSegmentToURLPatternFormat(path), module, _children);
      });
    });

    const agnosticRouteId =
      named.getIdentifier(HILLA_FILE_ROUTER_TYPES, 'AgnosticRoute') ??
      named.add(HILLA_FILE_ROUTER_TYPES, 'AgnosticRoute', true);
    const routeDeclaration = [
      ...this.#manager.imports.toCode(),
      ...ast`${errors.join('\n')}
const routes: readonly ${agnosticRouteId}[] = ${factory.createArrayLiteralExpression(routes, true)};
export default routes;`.source.statements,
    ];

    const file = createSourceFile(routeDeclaration, 'file-routes.ts');
    return printer.printFile(file);
  }

  /**
   * Create an abstract route creation function call. The nested function calls
   * create a route tree.
   *
   * @param path - The path of the route.
   * @param module - The module to build route from.
   * @param children - The list of child route call expressions.
   */
  #createRouteData(
    path: string,
    module: LazyModule | RegularModule | undefined,
    children: readonly CallExpression[] | undefined,
  ): CallExpression {
    const { named } = this.#manager.imports;

    const createRouteId =
      named.getIdentifier(HILLA_FILE_ROUTER_RUNTIME, 'createRoute') ??
      named.add(HILLA_FILE_ROUTER_RUNTIME, 'createRoute');

    let component: PropertyAccessExpression | CallExpression | undefined;
    let config: PropertyAccessExpression | ObjectLiteralExpression | string | undefined;

    if (isLazyModule(module)) {
      const { children: _c, params: _p, ...viewConfig } = module.config;

      component = ast`${module.lazyId}(() => import('${module.path}')`.node as CallExpression;
      config = Object.keys(viewConfig).length > 0 ? JSON.stringify(viewConfig) : '';
    } else if (module) {
      component = module.componentId;
      config = module.flowLayout
        ? (ast`const a = %{ { ...${module.configId}, flowLayout: ${module.flowLayout.toString()} } }%`
            .node as ObjectLiteralExpression)
        : module.configId;
    }

    const _children = children ? factory.createArrayLiteralExpression(children, true) : '';

    // ```ts
    // createRoute("grandparent", {...grandparentConfig, flowLayout: true}, [
    //   createRoute("parent", ParentPage, parentConfig, [
    //     createRoute("child", lazy(() => import('../ChildPage.js')), { title: 'Child Page' }),
    //   ]),
    // ]),
    // ```
    return ast`${createRouteId}("${path}", ${component ?? ''}, ${config ?? ''}, ${_children})`.node as CallExpression;
  }
}

export default function createRoutesFromMeta(
  views: readonly RouteMeta[],
  viewConfigs: readonly ServerViewConfig[],
  urls: RuntimeFileUrls,
): string {
  return new RouteFromMetaProcessor(views, viewConfigs, urls).process();
}
