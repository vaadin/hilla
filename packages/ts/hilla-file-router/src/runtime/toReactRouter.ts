import { createElement } from 'react';
import type { RouteObject } from 'react-router';
import { type AgnosticRoute, convertComponentNameToTitle, type RouteModule, transformRoute } from './utils.js';

/**
 * Transforms generated routes into a format that can be used by React Router.
 *
 * @param routes - Generated routes
 */
export function toReactRouter(routes: AgnosticRoute<RouteModule>): RouteObject {
  return transformRoute(
    routes,
    (route) => route.children?.values(),
    ({ path, module }, children) => {
      const title = module?.config?.title ?? convertComponentNameToTitle(module?.default);

      return {
        path: module?.config?.route ?? path,
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
