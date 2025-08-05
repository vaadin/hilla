import { createElement, type ComponentType } from 'react';
import type { RouteObject } from 'react-router';
import { transformTree } from '../../shared/transformTree.js';
import { getHandleFlag, RouteHandleFlag } from './utils.js';

/**
 * Splits out the server-side route tree based on the {@link ViewConfiguration.flowLayout}
 * flag, and wraps them in the provided server layout component.
 *
 * @remarks
 * Internally, routes are categorized into three groups:
 * - **Server routes**: Routes with `flowLayout` flag explicitly set to `true`,
 * or routes whose children have the flag enabled.
 * - **Client routes**: Routes with `flowLayout` flag explicitly set to `false`,
 * or routes with client-side children.
 * - **Unknown routes**: Routes without explicit flags that inherit behavior
 * from their parent context.
 *
 * Server routes get the {@link RouteHandleFlag.IGNORE_FALLBACK} flag set to
 * prevent fallback route interference. Client routes are preserved as-is.
 *
 * @param originalRoutes - The current route tree to process.
 * @param component - The layout component to wrap around server routes.
 *
 * @returns A new route configuration with the layout applied to server routes.
 */
export default function mergeLayout(
  originalRoutes: readonly RouteObject[] | undefined,
  component: ComponentType,
): readonly RouteObject[] | undefined {
  if (!originalRoutes) {
    return originalRoutes;
  }

  type Groups<T = readonly RouteObject[]> = Readonly<{
    server: T;
    client: T;
    unknown: T;
  }>;

  const result = transformTree<readonly RouteObject[], Groups>(originalRoutes, null, (routes, next) =>
    // Group routes onto:
    // - a server routes group,
    // - a client routes group,
    // - a group of routes which alignment cannot be classified; their
    // destination depends on the parent route alignment.
    routes.reduce<Groups<RouteObject[]>>(
      (groups, route) => {
        const { server, client, unknown } = next(route.children ?? []);

        const flag = getHandleFlag(route, RouteHandleFlag.FLOW_LAYOUT);

        // If the route has `flowLayout` flag explicitly enabled, it goes to
        // the server group. The children are also affected by the flag
        // unless they have it explicitly disabled.
        if (flag === true) {
          groups.server.push({
            ...route,
            children: route.children ? [...server, ...unknown] : undefined,
          } as RouteObject);
        } else if (server.length > 0) {
          // Even if the route doesn't have the flag, it goes to the server
          // group if any of the children has the flag enabled.
          groups.server.push({
            ...route,
            children: route.children ? server : undefined,
          } as RouteObject);
        }

        // If the route has `flowLayout` flag explicitly disabled, it goes
        // to the client group. The route children are not affected by the
        // flag.
        if (flag === false || client.length > 0) {
          groups.client.push({
            ...route,
            children: route.children ? client : undefined,
          } as RouteObject);
        }

        // The route without the flag goes to the `unknown` group. Then it
        // will be moved to either server or client group based on the parent
        // route.
        if (flag === undefined && (groups.server.every(({ path }) => path !== route.path) || unknown.length > 0)) {
          groups.unknown.push({
            ...route,
            children: route.children ? unknown : undefined,
          } as RouteObject);
        }

        return groups;
      },
      { server: [], client: [], unknown: [] },
    ),
  );

  return [
    ...(result.server.length
      ? [
          // Wrap the server routes with the layout component and make it ignore
          // fallback, so that the server routes are not affected by it.
          {
            element: createElement(component),
            children: result.server as RouteObject[],
            handle: {
              [RouteHandleFlag.IGNORE_FALLBACK]: true,
            },
          },
        ]
      : []),
    // The client routes are preserved without wrapping.
    ...result.client,
    // The unknown routes are considered as client routes.
    ...result.unknown,
  ];
}
