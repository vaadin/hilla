import { relative, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { template, transform as transformer } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ts, { type CallExpression, type Identifier, type StringLiteral, type VariableStatement } from 'typescript';

import { transformTree } from '../shared/transformTree.js';
import type { RouteMeta } from './collectRoutesFromFS.js';
import type { RuntimeFileUrls } from './generateRuntimeFiles.js';
import { convertFSRouteSegmentToURLPatternFormat } from './utils.js';

const printer = ts.createPrinter({ newLine: ts.NewLineKind.LineFeed });

const extensions = ['.ts', '.tsx', '.js', '.jsx'];

class RouteFromMetaProcessor {
  readonly #manager: DependencyManager;
  readonly #views: readonly RouteMeta[];

  constructor(views: readonly RouteMeta[], { code: codeFile }: RuntimeFileUrls) {
    this.#views = views;

    const codeDir = new URL('./', codeFile);
    this.#manager = new DependencyManager(new PathManager({ extension: '.js', relativeTo: codeDir }));
  }

  /**
   * Loads all the files from the received metadata and creates a framework-agnostic route tree.
   *
   * @param views - The abstract route tree.
   * @param generatedDir - The directory where the generated view file will be stored.
   */
  process(): string {
    const {
      paths,
      imports: { named, namespace },
    } = this.#manager;
    const errors: string[] = [];

    const routes = transformTree<readonly RouteMeta[], readonly CallExpression[]>(this.#views, (metas, next) => {
      errors.push(
        ...metas
          .map((route) => route.path)
          .filter((item, index, arr) => arr.indexOf(item) !== index)
          .map((dup) => `console.error("Two views share the same path: ${dup}");`),
      );

      return metas.map(({ file, layout, path, children, flowLayout }) => {
        let _children: readonly CallExpression[] | undefined;

        if (children) {
          _children = next(...children);
        }

        let mod: Identifier | undefined;
        if (file) {
          const extension = extensions.find((ext) => file.pathname.endsWith(ext));
          mod = namespace.add(paths.createRelativePath(file, extension), `Page`);
        } else if (layout) {
          const extension = extensions.find((ext) => layout.pathname.endsWith(ext));
          mod = namespace.add(paths.createRelativePath(layout, extension), `Layout`);
        }

        const extension = flowLayout ? { flowLayout } : undefined;

        return this.#createRouteData(convertFSRouteSegmentToURLPatternFormat(path), mod, extension, _children);
      });
    });

    const agnosticRouteId =
      named.getIdentifier('@vaadin/hilla-file-router/types.js', 'AgnosticRoute') ??
      named.add('@vaadin/hilla-file-router/types.js', 'AgnosticRoute', true);

    let routeDeclaration = template(
      `${errors.join('\n')}

const routes: readonly AGNOSTIC_ROUTE[] = ROUTE;

export default routes;
`,
      [
        transformer((node) =>
          ts.isIdentifier(node) && node.text === 'ROUTE' ? ts.factory.createArrayLiteralExpression(routes, true) : node,
        ),
        transformer((node) => (ts.isIdentifier(node) && node.text === 'AGNOSTIC_ROUTE' ? agnosticRouteId : node)),
      ],
    );

    routeDeclaration = [...this.#manager.imports.toCode(), ...routeDeclaration];

    const file = createSourceFile(routeDeclaration, 'file-routes.ts');
    return printer.printFile(file);
  }

  /**
   * Create an abstract route creation function call. The nested function calls
   * create a route tree.
   *
   * @param path - The path of the route.
   * @param mod - The name of the route module imported as a namespace.
   * @param children - The list of child route call expressions.
   */
  #createRouteData(
    path: string,
    mod: Identifier | undefined,
    extension?: Readonly<Record<string, unknown>>,
    children?: readonly CallExpression[],
  ): CallExpression {
    const { named } = this.#manager.imports;

    let extendModuleId: Identifier | undefined;
    let modNode = '';

    if (mod) {
      if (extension) {
        extendModuleId =
          named.getIdentifier('@vaadin/hilla-file-router/runtime.js', 'extendModule') ??
          named.add('@vaadin/hilla-file-router/runtime.js', 'extendModule');
        modNode = `, EXTEND_MODULE(MOD, ${JSON.stringify(extension)})`;
      } else {
        modNode = `, MOD`;
      }
    }

    const createRouteId =
      named.getIdentifier('@vaadin/hilla-file-router/runtime.js', 'createRoute') ??
      named.add('@vaadin/hilla-file-router/runtime.js', 'createRoute');

    return template(
      `const route = CREATE_ROUTE("${path}", ${modNode}${children ? `, CHILDREN` : ''})`,
      ([statement]) => (statement as VariableStatement).declarationList.declarations[0].initializer as CallExpression,
      [
        transformer((node) =>
          ts.isIdentifier(node) && node.text === 'CHILDREN'
            ? ts.factory.createArrayLiteralExpression(children, true)
            : node,
        ),
        transformer((node) => (ts.isIdentifier(node) && node.text === 'MOD' ? mod : node)),
        transformer((node) => (ts.isIdentifier(node) && node.text === 'EXTEND_MODULE' ? extendModuleId : node)),
        transformer((node) => (ts.isIdentifier(node) && node.text === 'CREATE_ROUTE' ? createRouteId : node)),
      ],
    );
  }
}

export default function createRoutesFromMeta(views: readonly RouteMeta[], urls: RuntimeFileUrls): string {
  return new RouteFromMetaProcessor(views, urls).process();
}
