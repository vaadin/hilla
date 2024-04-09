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
 */
export default async function createViewConfigJson(views: RouteMeta): Promise<string> {
  const res = await transformTree<RouteMeta, ServerViewConfig>(
    views,
    (route) => route.children?.values(),
    async ({ path, file, layout }, children) => {
      if (!file && !layout) {
        return {
          route: path,
          params: extractParameterFromRouteSegment(path),
          children,
        } as const;
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
          }
        } else if (node.getText(sourceFile).startsWith('export default')) {
          waitingForIdentifier = true;
        } else if (waitingForIdentifier && ts.isIdentifier(node)) {
          componentName = node.text;
          break;
        }
      }

      return {
        route: convertFSRouteSegmentToURLPatternFormat(path),
        ...config,
        params: extractParameterFromRouteSegment(config?.route ?? path),
        title: config?.title ?? convertComponentNameToTitle(componentName),
        children,
      } as const;
    },
  );

  return JSON.stringify(res);
}
