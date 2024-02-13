import { createElement } from 'react';
import type { RouteObject } from 'react-router';
import {
  adjustRouteConfig,
  type AgnosticRoute,
  extractComponentName,
  type RouteModule,
  transformRoute,
} from './utils.js';

/**
 * Transforms generated routes into a format that can be used by React Router.
 *
 * @param routes - Generated routes
 */
export function toReactRouter(routes: AgnosticRoute<RouteModule>): RouteObject {
  return transformRoute(
    routes,
    (route) => route.children?.values(),
    ({ path, module }, children) =>
      ({
        path: module?.config?.route ?? path,
        element: module?.default ? createElement(module.default) : undefined,
        children: children.length > 0 ? (children as RouteObject[]) : undefined,
        handle: adjustRouteConfig(module?.config, extractComponentName(module?.default)),
      }) satisfies RouteObject,
  );
}
