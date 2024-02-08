import type { UIMatch } from '@remix-run/router';
import { type ComponentType, createElement } from 'react';
import { type RouteObject, useMatches } from 'react-router';
import {
  type AgnosticRoute,
  transformRoute,
  adjustViewTitle,
  type ViewConfig,
  extractComponentName,
} from './runtime/utils.js';

export type RouteModule<P = object> = Readonly<{
  default: ComponentType<P>;
  config?: ViewConfig;
}>;

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
        path,
        element: module?.default ? createElement(module.default) : undefined,
        children: children.length > 0 ? (children as RouteObject[]) : undefined,
        handle: adjustViewTitle(module?.config, extractComponentName(module?.default)),
      }) satisfies RouteObject,
  );
}

/**
 * Hook to return the {@link ViewConfig} for the current route.
 */
export function useViewConfig<M extends ViewConfig>(): M | undefined {
  const matches = useMatches() as ReadonlyArray<UIMatch<unknown, M>>;
  return matches[matches.length - 1]?.handle;
}
