import { extname, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import { template, transform as transformer } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import ts, {
  type ImportDeclaration,
  type ObjectLiteralExpression,
  type StringLiteral,
  type VariableStatement,
} from 'typescript';
import { transformRoute } from '../runtime/utils.js';
import type { RouteMeta } from './collectRoutesFromFS.js';
import { convertFSPatternToURLPatternString } from './utils.js';

const printer = ts.createPrinter({ newLine: ts.NewLineKind.LineFeed });

function relativize(url: URL, generatedDir: URL): string {
  const result = relative(fileURLToPath(generatedDir), fileURLToPath(url));

  if (!result.startsWith('.')) {
    return `./${result}`;
  }

  return result;
}

function createImport(mod: string, file: string): ImportDeclaration {
  const path = `${file.substring(0, file.lastIndexOf('.'))}.js`;
  return template(`import * as ${mod} from '${path}';\n`, ([statement]) => statement as ts.ImportDeclaration);
}

function createRouteData(
  path: string,
  mod: string | undefined,
  children: readonly ObjectLiteralExpression[],
): ObjectLiteralExpression {
  return template(
    `const route = {
  path: '${path}',
  ${mod ? `module: ${mod}` : ''}
  ${children.length > 0 ? `children: CHILDREN,` : ''}
}`,
    ([statement]) =>
      (statement as VariableStatement).declarationList.declarations[0].initializer as ObjectLiteralExpression,
    [
      transformer((node) =>
        ts.isIdentifier(node) && node.text === 'CHILDREN' ? ts.factory.createArrayLiteralExpression(children) : node,
      ),
    ],
  );
}

export default function createRoutesFromMeta(views: RouteMeta, generatedDir: URL): string {
  const imports: ImportDeclaration[] = [];
  let id = 0;

  const routes = transformRoute<RouteMeta, ObjectLiteralExpression>(
    views,
    (view) => view.children.values(),
    ({ file, layout, path }, children) => {
      const currentId = id;
      id += 1;

      let mod: string | undefined;
      if (file) {
        mod = `Page${currentId}`;
        imports.push(createImport(mod, relativize(file, generatedDir)));
      } else if (layout) {
        mod = `Layout${currentId}`;
        imports.push(createImport(mod, relativize(layout, generatedDir)));
      }

      return createRouteData(convertFSPatternToURLPatternString(path), mod, children);
    },
  );

  const routeDeclaration = template(
    `import a from 'IMPORTS';

const routes = ROUTE;

export default routes;
`,
    [
      transformer((node) =>
        ts.isImportDeclaration(node) && (node.moduleSpecifier as StringLiteral).text === 'IMPORTS' ? imports : node,
      ),
      transformer((node) => (ts.isIdentifier(node) && node.text === 'ROUTE' ? routes : node)),
    ],
  );

  const file = createSourceFile(routeDeclaration, 'views.ts');

  return printer.printFile(file);
}
