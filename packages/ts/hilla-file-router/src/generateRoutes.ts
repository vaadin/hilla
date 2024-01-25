import { relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import { template, transform as transformer } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import ts, {
  type ImportDeclaration,
  type ObjectLiteralExpression,
  type StringLiteral,
  type VariableStatement,
} from 'typescript';
import type { RouteMeta } from './collectRoutes.js';
import { processPattern, transformRoute } from './utils.js';

const printer = ts.createPrinter({ newLine: ts.NewLineKind.LineFeed });

function relativize(url: URL, outDir: URL): string {
  const result = relative(fileURLToPath(outDir), fileURLToPath(url));

  if (!result.startsWith('.')) {
    return `./${result}`;
  }

  return result;
}

function createImport(component: string, file: string): ImportDeclaration {
  return template(`import ${component} from '${file}';\n`, ([statement]) => statement as ts.ImportDeclaration);
}

function createRouteData(
  path: string,
  component: string | undefined,
  children: readonly ObjectLiteralExpression[],
): ObjectLiteralExpression {
  return template(
    `const route = {
  path: '${path}',
  ${component ? `component: ${component}` : ''}
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

export default function generateRoutes(views: RouteMeta, outDir: URL): string {
  const imports: ImportDeclaration[] = [];
  let id = 0;

  const routes = transformRoute<RouteMeta, ObjectLiteralExpression>(
    views,
    (view) => view.children.values(),
    ({ file, layout, path }, children) => {
      const currentId = id;
      id += 1;

      let component: string | undefined;
      if (file) {
        component = `Page${currentId}`;
        imports.push(createImport(component, relativize(file, outDir)));
      } else if (layout) {
        component = `Layout${currentId}`;
        imports.push(createImport(component, relativize(layout, outDir)));
      }

      return createRouteData(processPattern(path), component, children);
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
