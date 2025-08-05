import type { RouteObject } from 'react-router';
import { transformTree } from '../../shared/transformTree.js';
import { getHandleFlag, RouteHandleFlag } from './utils.js';

/**
 * Processes a tree of route objects and splits them into two separate trees:
 * - routes marked with {@link ViewConfiguration.skipLayout} flag that require
 * no layout rendering,
 * - regular routes that should render with their layouts.
 *
 * For routes with the {@link ViewConfiguration.skipLayout} flag, the layout
 * element is removed. The resulting routes are also marked with
 * {@link ViewConfiguration.ignoreFallback} flag to prevent rendering
 * server-side layouts for them.
 *
 * @param originalRoutes - The current route tree to process.
 *
 * @returns A new array containing reorganized routes with skipped routes first (wrapped
 * in an ignore-fallback container) followed by regular routes.
 */
export default function mergeSkipLayouts(
  originalRoutes: readonly RouteObject[] | undefined,
): readonly RouteObject[] | undefined {
  if (!originalRoutes) {
    return originalRoutes;
  }

  type Groups<T = readonly RouteObject[]> = Readonly<{
    skipped: T;
    regular: T;
  }>;

  const result = transformTree<readonly RouteObject[], Groups>(originalRoutes, null, (routes, next) =>
    // Split a single routes list onto two separate lists.
    routes.reduce<Groups<RouteObject[]>>(
      (lists, route) => {
        // If the route has `skipLayout` flag, it goes to the `skipped` list.
        if (getHandleFlag(route, RouteHandleFlag.SKIP_LAYOUTS)) {
          lists.skipped.push(route);
          return lists;
        }

        // If the route is leaf, it goes to the `regular` list.
        if (!route.children?.length) {
          lists.regular.push(route);
          return lists;
        }

        // As of children, we have to split them into two lists as well.
        const { skipped, regular } = next(route.children ?? []);

        // If we have `skipped` list of children, we have to remove the
        // `element` property of the router to prevent the layout from
        // rendering. Then, we add the current route to the `skipped` list.
        if (skipped.length > 0) {
          const { element, ...rest } = route;

          lists.skipped.push({
            ...rest,
            children: skipped,
          } as RouteObject);
        }

        // In case of `regular` children, we just add the current route to
        // the `regular` list if there are any children.
        if (regular.length > 0) {
          lists.regular.push({
            ...route,
            children: regular,
          } as RouteObject);
        }

        return lists;
      },
      { skipped: [], regular: [] },
    ),
  );

  // We don't need a fallback for the skipped routes, so we have to wrap
  // them with the route with the `IGNORE_FALLBACK` flag.
  return [
    ...(result.skipped.length
      ? [
          {
            children: result.skipped as RouteObject[],
            handle: {
              [RouteHandleFlag.IGNORE_FALLBACK]: true,
            },
          },
        ]
      : []),
    ...result.regular,
  ];
}
