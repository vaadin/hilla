import { readFile } from 'node:fs/promises';
import { Script } from 'node:vm';
import ts, { type Node } from 'typescript';
import type { RouteMeta } from './collectRoutes.js';
import { prepareConfig, processPattern, type ViewConfig } from './utils.js';

function* traverse(
  views: RouteMeta,
  parents: readonly RouteMeta[] = [],
): Generator<readonly RouteMeta[], undefined, undefined> {
  const chain = [...parents, views];

  if (views.children.length === 0) {
    yield chain;
  }

  for (const child of views.children) {
    yield* traverse(child, chain);
  }
}

type RouteModule = Readonly<{
  default: unknown;
  config?: ViewConfig;
}>;

function* walkAST(node: Node): Generator<Node> {
  yield node;

  for (const child of node.getChildren()) {
    yield* walkAST(child);
  }
}

export default async function generateJson(views: RouteMeta, exportName: string): Promise<string> {
  const res = await Promise.all(
    Array.from(traverse(views), async (branch) => {
      const configs = await Promise.all(
        branch
          .filter(({ file, layout }) => !!file || !!layout)
          .map(({ file, layout }) => file ?? layout!)
          .map(async (path) => {
            const file = ts.createSourceFile('f.ts', await readFile(path, 'utf8'), ts.ScriptTarget.ESNext, true);
            let config: ViewConfig | undefined;
            let waitingForIdentifier = false;
            let componentName: string | undefined;

            for (const node of walkAST(file)) {
              if (ts.isVariableDeclaration(node) && ts.isIdentifier(node.name) && node.name.text === exportName) {
                if (node.initializer && ts.isObjectLiteralExpression(node.initializer)) {
                  const code = node.initializer.getText(file);
                  const script = new Script(`(${code})`);
                  config = script.runInThisContext() as ViewConfig;
                }
              } else if (node.getText(file).includes('export default')) {
                waitingForIdentifier = true;
              } else if (waitingForIdentifier && ts.isIdentifier(node)) {
                componentName = node.text;
              }
            }

            return prepareConfig(config, componentName);
          }),
      );

      const key = branch.map(({ path }) => processPattern(path)).join('/');
      const value = configs[configs.length - 1];

      return [key, value] satisfies readonly [string, ViewConfig | undefined];
    }),
  );

  return JSON.stringify(Object.fromEntries(res));
}
