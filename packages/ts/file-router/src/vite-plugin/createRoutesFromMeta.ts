import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import { DefaultImportManager } from '@vaadin/hilla-generator-utils/dependencies/ImportManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import StatementRecordManager, {
  type StatementRecord,
} from '@vaadin/hilla-generator-utils/dependencies/StatementRecordManager.js';
import ast from 'tsc-template';
import {
  type CallExpression,
  createPrinter,
  factory,
  type Identifier,
  NewLineKind,
  type VariableStatement,
} from 'typescript';
import type { ServerViewConfig } from '../shared/internal.js';
import { transformTree } from '../shared/transformTree.js';
import type { RouteMeta } from './collectRoutesFromFS.js';
import type { RuntimeFileUrls } from './generateRuntimeFiles.js';
import { convertFSRouteSegmentToURLPatternFormat, strip } from './utils.js';

const printer = createPrinter({ newLine: NewLineKind.LineFeed });

const fileExtensions = ['.ts', '.tsx', '.js', '.jsx'];

class LazyImportManager extends StatementRecordManager<VariableStatement> {
  readonly #lazyId: Identifier;

  readonly #manager: DefaultImportManager;

  get size(): number {
    return this.#manager.size;
  }

  constructor(collator: Intl.Collator, lazyId: Identifier) {
    super(collator);
    this.#manager = new DefaultImportManager(collator);
    this.#lazyId = lazyId;
  }

  add(path: string, name: string, uniqueId?: Identifier): Identifier {
    return this.#manager.add(path, name, false, uniqueId);
  }

  override clear(): void {
    this.#manager.clear();
  }

  override *statementRecords(): IterableIterator<StatementRecord<VariableStatement>> {
    for (const [path, id] of this.#manager) {
      yield [
        path,
        ast`const ${id} = ${this.#lazyId}(() => import('${path}'));`.source.statements[0] as VariableStatement,
      ];
    }
  }
}

function isLazy(path: string, config: ServerViewConfig, rootConfig: readonly ServerViewConfig[]): boolean {
  const strippedPath = strip(path);

  if (rootConfig.includes(config) && (strippedPath === '' || strippedPath === 'login')) {
    return false;
  }

  return config.lazy ?? true;
}

class RouteFromMetaProcessor {
  readonly #lazyImportManager: LazyImportManager;
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
    this.#lazyImportManager = new LazyImportManager(
      this.#manager.imports.collator,
      this.#manager.imports.named.add('react', 'lazy'),
    );
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

        let mod: Identifier | undefined;
        if (file) {
          const fileExt = fileExtensions.find((ext) => file.pathname.endsWith(ext));
          const relativePath = paths.createRelativePath(file, fileExt);
          mod = isLazy(path, config, this.#configs)
            ? this.#lazyImportManager.add(relativePath, `Page`)
            : namespace.add(relativePath, `Page`);
        } else if (layout) {
          const fileExt = fileExtensions.find((ext) => layout.pathname.endsWith(ext));
          const relativePath = paths.createRelativePath(layout, fileExt);
          mod = isLazy(path, config, this.#configs)
            ? this.#lazyImportManager.add(relativePath, `Layout`)
            : namespace.add(paths.createRelativePath(layout, fileExt), `Layout`);
        }

        const moduleExtension = flowLayout ? { flowLayout } : undefined;

        return this.#createRouteData(convertFSRouteSegmentToURLPatternFormat(path), mod, moduleExtension, _children);
      });
    });

    if (this.#lazyImportManager.size === 0) {
      named.remove('react', 'lazy');
    }

    const agnosticRouteId =
      named.getIdentifier('@vaadin/hilla-file-router/types.js', 'AgnosticRoute') ??
      named.add('@vaadin/hilla-file-router/types.js', 'AgnosticRoute', true);
    const routeDeclaration = [
      ...this.#manager.imports.toCode(),
      ...this.#lazyImportManager.toCode(),
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
    mod: Identifier | undefined,
    extension?: Readonly<Record<string, unknown>>,
    children?: readonly CallExpression[],
  ): CallExpression {
    const { named } = this.#manager.imports;

    let modNode: Identifier | CallExpression | undefined = mod;

    if (extension) {
      const extendModuleId =
        named.getIdentifier('@vaadin/hilla-file-router/runtime.js', 'extendModule') ??
        named.add('@vaadin/hilla-file-router/runtime.js', 'extendModule');

      // `extendModule(Page, { flowLayout: true }),`
      modNode = ast`${extendModuleId}(${mod ?? 'null'}, ${JSON.stringify(extension)})`.node as CallExpression;
    }

    const createRouteId =
      named.getIdentifier('@vaadin/hilla-file-router/runtime.js', 'createRoute') ??
      named.add('@vaadin/hilla-file-router/runtime.js', 'createRoute');

    // ```ts
    // createRoute("parent", extendModule(Layout, {flowLayout: true}), [
    //   createRoute("child", Page),
    // ]),
    // ```
    return ast`${createRouteId}("${path}", ${modNode ?? ''} ${children ? factory.createArrayLiteralExpression(children, true) : ''})`
      .node as CallExpression;
  }
}

export default function createRoutesFromMeta(
  views: readonly RouteMeta[],
  viewConfigs: readonly ServerViewConfig[],
  urls: RuntimeFileUrls,
): string {
  return new RouteFromMetaProcessor(views, viewConfigs, urls).process();
}
