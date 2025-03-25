import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ast from 'tsc-template';
import { type CallExpression, createPrinter, factory, type Identifier, NewLineKind } from 'typescript';
import { isServerViewConfig, type ServerViewConfig } from '../shared/internal.js';
import { transformTree } from '../shared/transformTree.js';
import type { RouteMeta } from './collectRoutesFromFS.js';
import type { RuntimeFileUrls } from './generateRuntimeFiles.js';
import { convertFSRouteSegmentToURLPatternFormat, strip } from './utils.js';

const printer = createPrinter({ newLine: NewLineKind.LineFeed });

const fileExtensions = ['.ts', '.tsx', '.js', '.jsx'];

const HILLA_FILE_ROUTER = '@vaadin/hilla-file-router';
const HILLA_FILE_ROUTER_RUNTIME = `${HILLA_FILE_ROUTER}/runtime.js`;
const HILLA_FILE_ROUTER_TYPES = `${HILLA_FILE_ROUTER}/types.js`;

type ModuleExtension = Readonly<Record<string, unknown>>;

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
      imports: { named, namespace },
    } = this.#manager;
    const errors: string[] = [];

    const routes = transformTree<
      [metas: readonly RouteMeta[], configs: readonly ServerViewConfig[]],
      readonly CallExpression[]
    >([this.#views, this.#configs], null, ([metas, configs], next) => {
      errors.push(
        ...metas
          .map((route) => route.path)
          .filter((item, index, arr) => arr.indexOf(item) !== index)
          .map((dup) => `console.error("Two views share the same path: ${dup}");`),
      );

      return metas.map(({ file, layout, path, children, flowLayout }, index) => {
        const config = configs[index];
        let _children: readonly CallExpression[] | undefined;

        if (children) {
          // Assuming that if we have `children` in the route, we also have
          // `children` in the config.
          _children = next([children, config.children!]);
        }

        let mod: Identifier | string | undefined;
        const isLazy = this.#isLazy(path, config);
        if (file) {
          const fileExt = fileExtensions.find((ext) => file.pathname.endsWith(ext));
          const relativePath = paths.createRelativePath(file, fileExt);
          mod = isLazy ? relativePath : namespace.add(relativePath, `Page`);
        } else if (layout) {
          const fileExt = fileExtensions.find((ext) => layout.pathname.endsWith(ext));
          const relativePath = paths.createRelativePath(layout, fileExt);
          mod = isLazy ? relativePath : namespace.add(paths.createRelativePath(layout, fileExt), `Layout`);
        }

        // If the route is lazy, flowLayout is already included in the config.
        const configOrExtension = isLazy ? config : flowLayout ? { flowLayout } : undefined;

        return this.#createRouteData(convertFSRouteSegmentToURLPatternFormat(path), mod, _children, configOrExtension);
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
   * @param mod - The name of the route module imported as a namespace.
   * @param extension - The object that contains specific features of the
   *                    module.
   * @param children - The list of child route call expressions.
   */
  #createRouteData(
    path: string,
    mod: Identifier | string | undefined,
    children: readonly CallExpression[] | undefined,
    extension: ModuleExtension | undefined,
  ): CallExpression;
  /**
   * Create an abstract route creation function call. The nested function calls
   * create a route tree.
   *
   * @param path - The path of the route.
   * @param mod - The name of the route module imported as a namespace.
   * @param config - The ViewConfig object for the route.
   * @param children - The list of child route call expressions.
   */
  #createRouteData(
    path: string,
    mod: string | undefined,
    children: readonly CallExpression[] | undefined,
    config: ServerViewConfig,
  ): CallExpression;
  #createRouteData(
    path: string,
    mod: Identifier | string | undefined,
    children: readonly CallExpression[] | undefined,
    configOrExtension: ModuleExtension | ServerViewConfig | undefined,
  ): CallExpression {
    const { named } = this.#manager.imports;

    let modNode: Identifier | string | CallExpression | undefined = mod;

    if (mod) {
      if (isServerViewConfig(configOrExtension)) {
        const createLazyModuleId =
          named.getIdentifier(HILLA_FILE_ROUTER_RUNTIME, 'createLazyModule') ??
          named.add(HILLA_FILE_ROUTER_RUNTIME, 'createLazyModule');

        const { children: _c, params: _p, brand: _b, ...viewConfig } = configOrExtension;

        // ```ts
        // createLazyModule(() => import('../LazyPage.js'), { title: 'Lazy Page' }));
        // ```
        modNode =
          ast`${createLazyModuleId}(() => import('${mod as string}'), ${Object.keys(viewConfig).length > 0 ? JSON.stringify(viewConfig) : ''})`
            .node as CallExpression;
      } else if (configOrExtension) {
        const extendModuleId =
          named.getIdentifier(HILLA_FILE_ROUTER_RUNTIME, 'extendModule') ??
          named.add(HILLA_FILE_ROUTER_RUNTIME, 'extendModule');

        // `extendModule(Page, { flowLayout: true }),`
        modNode = ast`${extendModuleId}(${mod}, ${JSON.stringify(configOrExtension)})`.node as CallExpression;
      }
    }

    const createRouteId =
      named.getIdentifier(HILLA_FILE_ROUTER_RUNTIME, 'createRoute') ??
      named.add(HILLA_FILE_ROUTER_RUNTIME, 'createRoute');

    // ```ts
    // createRoute("grandparent", extendModule(Layout, {flowLayout: true}), [
    //   createRoute("parent", ParentPage, [
    //     createRoute("child", createLazyModule(() => import('../ChildPage.js'), { title: 'Child Page' }))
    //   ]),
    // ]),
    // ```
    return ast`${createRouteId}("${path}", ${modNode ?? ''}, ${children && children.length > 0 ? factory.createArrayLiteralExpression(children, true) : ''})`
      .node as CallExpression;
  }

  #isLazy(path: string, config: ServerViewConfig): boolean {
    const strippedPath = strip(path);

    if (this.#configs.includes(config) && (strippedPath === '' || strippedPath === 'login')) {
      return false;
    }

    return config.lazy ?? true;
  }
}

export default function createRoutesFromMeta(
  views: readonly RouteMeta[],
  viewConfigs: readonly ServerViewConfig[],
  urls: RuntimeFileUrls,
): string {
  return new RouteFromMetaProcessor(views, viewConfigs, urls).process();
}
