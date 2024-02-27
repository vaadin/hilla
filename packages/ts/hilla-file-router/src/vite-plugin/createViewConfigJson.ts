import { readFile } from 'node:fs/promises';
import { Script } from 'node:vm';
import ts, { type Node } from 'typescript';
import { convertComponentNameToTitle } from '../shared/convertComponentNameToTitle.js';
import traverse from '../shared/traverse.js';
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
  const res = await Promise.all(
    Array.from(traverse(views), async (branch) => {
      const configs = await Promise.all(
        branch.map(async ({ path, file, layout }): Promise<[string, ViewConfig]> => {
          if (!file && !layout) {
            return [
              convertFSRouteSegmentToURLPatternFormat(path),
              { params: extractParameterFromRouteSegment(path) },
            ] as const;
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
          const _path = config?.route ?? path;
          const pattern = convertFSRouteSegmentToURLPatternFormat(_path);

          return [
            pattern,
            {
              ...config,
              params: extractParameterFromRouteSegment(_path),
              title: config?.title ?? convertComponentNameToTitle(componentName),
            },
          ] as const;
        }),
      );

      const key = configs.map(([path]) => path).join('/');
      const params = configs.reduce((acc, [, { params: p }]) => Object.assign(acc, p), {});
      const [, value] = configs[configs.length - 1];

      return [key, { ...value, params: Object.keys(params).length > 0 ? params : undefined }] as const;
    }),
  );

  return JSON.stringify(Object.fromEntries(res));
}