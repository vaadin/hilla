import { relative, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { template, transform as transformer } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import ts, {
  type CallExpression,
  type ImportDeclaration,
  type StringLiteral,
  type VariableStatement,
} from 'typescript';

import { transformTree } from '../shared/transformTree.js';
import type { RouteMeta } from './collectRoutesFromFS.js';
import type { RuntimeFileUrls } from './generateRuntimeFiles.js';
import { convertFSRouteSegmentToURLPatternFormat } from './utils.js';

const printer = ts.createPrinter({ newLine: ts.NewLineKind.LineFeed });

/**
 * Convert a file URL to a relative path from the generated directory.
 *
 * @param url - The file URL to convert.
 * @param generatedDir - The directory where the generated view file will be stored.
 */
function relativize(url: URL, generatedDir: URL): string {
  const result = relative(fileURLToPath(generatedDir), fileURLToPath(url)).replaceAll(sep, '/');

  if (!result.startsWith('.')) {
    return `./${result}`;
  }

  return result;
}

/**
 * Create an import declaration for a `views` module.
 *
 * @param mod - The name of the route module to import.
 * @param file - The file path of the module.
 */
function createImport(mod: string, file: string): ImportDeclaration {
  const path = `${file.substring(0, file.lastIndexOf('.'))}.js`;
  return template(`import * as ${mod} from '${path}';\n`, ([statement]) => statement as ts.ImportDeclaration);
}

/**
 * Create an abstract route creation function call. The nested function calls create a route tree.
 *
 * @param path - The path of the route.
 * @param mod - The name of the route module imported as a namespace.
 * @param children - The list of child route call expressions.
 */
function createRouteData(
  path: string,
  flowLayout: boolean | undefined,
  mod: string | undefined,
  children?: readonly CallExpression[],
): CallExpression {
  const serverLayout = flowLayout ?? false;
  return template(
    `const route = createRoute("${path}",${serverLayout}${mod ? `, ${mod}` : ''}${children ? `, CHILDREN` : ''})`,
    ([statement]) => (statement as VariableStatement).declarationList.declarations[0].initializer as CallExpression,
    [
      transformer((node) =>
        ts.isIdentifier(node) && node.text === 'CHILDREN'
          ? ts.factory.createArrayLiteralExpression(children, true)
          : node,
      ),
    ],
  );
}

/**
 * Loads all the files from the received metadata and creates a framework-agnostic route tree.
 *
 * @param views - The abstract route tree.
 * @param generatedDir - The directory where the generated view file will be stored.
 */
export default function createRoutesFromMeta(views: readonly RouteMeta[], { code: codeFile }: RuntimeFileUrls): string {
  const codeDir = new URL('./', codeFile);
  const imports: ImportDeclaration[] = [];
  const errors: string[] = [];
  let id = 0;

  const routes = transformTree<readonly RouteMeta[], readonly CallExpression[]>(views, (metas, next) => {
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

      const currentId = id;
      id += 1;

      let mod: string | undefined;
      if (file) {
        mod = `Page${currentId}`;
        imports.push(createImport(mod, relativize(file, codeDir)));
      } else if (layout) {
        mod = `Layout${currentId}`;
        imports.push(createImport(mod, relativize(layout, codeDir)));
      }

      return createRouteData(convertFSRouteSegmentToURLPatternFormat(path), flowLayout, mod, _children);
    });
  });

  // Prepend the import for `createRoute` if it was used
  if (imports.length > 0) {
    const createRouteImport = template(
      'import { createRoute } from "@vaadin/hilla-file-router/runtime.js";',
      ([statement]) => statement as ts.ImportDeclaration,
    );
    imports.unshift(createRouteImport);
  }

  imports.unshift(
    template(
      'import type { AgnosticRoute } from "@vaadin/hilla-file-router/types.js";',
      ([statement]) => statement as ts.ImportDeclaration,
    ),
  );

  const routeDeclaration = template(
    `import a from 'IMPORTS';

${errors.join('\n')}

const routes: readonly AgnosticRoute[] = ROUTE;

export default routes;
`,
    [
      transformer((node) =>
        ts.isImportDeclaration(node) && (node.moduleSpecifier as StringLiteral).text === 'IMPORTS' ? imports : node,
      ),
      transformer((node) =>
        ts.isIdentifier(node) && node.text === 'ROUTE' ? ts.factory.createArrayLiteralExpression(routes, true) : node,
      ),
    ],
  );

  const file = createSourceFile(routeDeclaration, 'file-routes.ts');
  return printer.printFile(file);
}
