import { readFile } from 'node:fs/promises';
import { Script } from 'node:vm';
import ts, { type Node } from 'typescript';
import { convertComponentNameToTitle } from '../shared/convertComponentNameToTitle.js';
import type { ServerViewConfig } from '../shared/internal.js';
import { transformTree } from '../shared/transformTree.js';
import type { ViewConfig } from '../types.js';
import type { RouteMeta } from './collectRoutesFromFS.js';
import { convertFSRouteSegmentToURLPatternFormat, extractParameterFromRouteSegment } from './utils.js';

/**
 * Walks the TypeScript AST using the deep-first search algorithm.
 *
 * @param node - The node to walk.
 */
function* walkAST(node: Node): Generator<Node> {
  yield node;

  for (const child of node.getChildren()) {
    yield* walkAST(child);
  }
}

/**
 * Creates a map of all leaf routes to their configuration. This file is used by the server to provide server-side
 * routes along with managing the client-side routes.
 *
 * @param views - The route metadata tree.
 * @returns A view configuration tree.
 */
export default async function createViewConfigJson(views: readonly RouteMeta[]): Promise<readonly ServerViewConfig[]> {
  return await transformTree<readonly RouteMeta[], Promise<readonly ServerViewConfig[]>>(
    views,
    null,
    async (routes, next) =>
      await Promise.all(
        routes.map(async ({ path, file, layout, children, flowLayout }) => {
          const newChildren = children ? await next(children) : undefined;

          if (!file && !layout) {
            return {
              route: convertFSRouteSegmentToURLPatternFormat(path),
              params: extractParameterFromRouteSegment(path),
              children: newChildren,
            } satisfies ServerViewConfig;
          }

          const sourceFile = ts.createSourceFile(
            'f.ts',
            await readFile(file ?? layout!, 'utf8'),
            ts.ScriptTarget.ESNext,
            true,
          );
          let config: ViewConfig | undefined;
          let waitingForIdentifier = false;
          let componentName: string | undefined;

          for (const node of walkAST(sourceFile)) {
            if (ts.isVariableDeclaration(node) && ts.isIdentifier(node.name) && node.name.text === 'config') {
              if (node.initializer && ts.isObjectLiteralExpression(node.initializer)) {
                const code = node.initializer.getText(sourceFile);
                const script = new Script(`(${code})`);
                config = script.runInThisContext() as ViewConfig;
                if (config.flowLayout === undefined) {
                  const copy = JSON.parse(JSON.stringify(config));
                  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
                  copy.flowLayout = flowLayout ?? false;
                  config = copy;
                }
              }
            } else if (node.getText(sourceFile).startsWith('export default')) {
              waitingForIdentifier = true;
            } else if (waitingForIdentifier && ts.isIdentifier(node)) {
              componentName = node.text;
              waitingForIdentifier = false;
            }
          }

          config ??= { flowLayout: flowLayout ?? false };

          let title: string;

          if (config.title) {
            ({ title } = config);
          } else {
            if (!componentName) {
              throw new Error(
                `The file "${String(file ?? layout!)}" must contain a default export of a component whose name will be used as title by default`,
              );
            }

            title = convertComponentNameToTitle(componentName);
          }

          return {
            route: convertFSRouteSegmentToURLPatternFormat(path),
            ...config,
            params: extractParameterFromRouteSegment(config.route ?? path),
            title,
            children: newChildren ?? (layout ? [] : undefined),
          } satisfies ServerViewConfig;
        }),
      ),
  );
}
