import { type ComponentType, createElement } from 'react';
import type { RouteObject } from 'react-router-dom';
import { convertComponentNameToTitle } from '../shared/convertComponentNameToTitle.js';
import { transformTreeSync } from '../shared/transformTree.js';
import type { AgnosticRoute } from '../types.js';

/**
 * Transforms framework-agnostic route tree into a format that can be used by React Router.
 *
 * @param routes - Generated routes
 *
 * @returns The React Router tree.
 */
export function toReactRouter(routes: AgnosticRoute<ComponentType>): RouteObject {
  return transformTreeSync(
    routes,
    (route) => route.children?.values(),
    ({ path, module }, children) => {
      const title = module?.config?.title ?? convertComponentNameToTitle(module?.default);

      return {
        path,
        element: module?.default ? createElement(module.default) : undefined,
        children: children.length > 0 ? (children as RouteObject[]) : undefined,
        handle: {
          ...module?.config,
          title,
        },
      } satisfies RouteObject;
    },
  );
}
