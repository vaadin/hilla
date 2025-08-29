import type { RouteObject } from 'react-router';
import { transformTree } from '../../shared/transformTree.js';
import { isWildcardRoute } from './utils.js';

export default function mergeDeepWildcard(
  originalRoutes: readonly RouteObject[] | undefined,
): readonly RouteObject[] | undefined {
  if (!originalRoutes) {
    return originalRoutes;
  }

  return transformTree<readonly RouteObject[], readonly RouteObject[]>(originalRoutes, null, (routes, next) =>
    routes.reduce<RouteObject[]>((acc, route, _, arr) => {
      const wildcard = arr.find(isWildcardRoute);
      const children = route.children
        ? next(
            wildcard && route.children.every((r) => !isWildcardRoute(r))
              ? [...route.children, wildcard]
              : route.children,
          )
        : undefined;

      acc.push({
        ...route,
        children,
      } as RouteObject);

      return acc;
    }, []),
  );
}
