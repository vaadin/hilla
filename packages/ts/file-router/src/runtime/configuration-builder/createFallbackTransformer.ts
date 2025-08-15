import { type ComponentType, createElement } from 'react';
import type { RouteObject, NonIndexRouteObject } from 'react-router';
import type { ViewConfig } from '../../types.js';
import {
  getHandleFlag,
  isIndexRoute,
  isOptionalRoute,
  isWildcardRoute,
  RouteHandleFlag,
  type RouteTransformer,
} from './utils.js';

/**
 * A tuple of fallback routes:
 * - A wildcard route (`path: '*'`) that renders the component for any unmatched
 * path.
 * - An index route (`index: true`) that renders the component for the empty
 * path.
 */
export type FallbackRoutes = readonly [notFoundFallback: RouteObject, indexFallback: RouteObject];

/**
 * Creates fallback routes for handling unmatched paths and index routes.
 *
 * @param component - The React component to render for the fallback routes
 * @param config - Optional view configuration to attach to the route handles
 *
 * @returns A tuple of fallback routes.
 */
export function createFallbackRoutes(component: ComponentType, config?: ViewConfig): FallbackRoutes {
  return [
    { path: '*', element: createElement(component), handle: config },
    { index: true, element: createElement(component), handle: config },
  ];
}

/**
 * Creates a route transformer that adds fallback routes to handle unmatched
 * paths.
 *
 * This transformer adds two types of fallback routes:
 * - A wildcard route (`path: '*'`) that renders the specified fallback
 * component for any unmatched path.
 * - An index fallback route (`index: true`) that renders the fallback component
 * for the empty path.
 *
 * The transformer logic determines which fallback to add based on the existing
 * child routes:
 * - If a wildcard child route already defined, only the index fallback is
 * added.
 * - If an index or optional child route exists, only the wildcard fallback is
 * added.
 * - Otherwise, both fallback routes are added.
 *
 * @param component - The React component to render as the fallback.
 * @param config - A view configuration of the fallback route if any.
 * @returns A route transformer function.
 */
export default function createFallbackTransformer([notFoundFallback, indexFallback]: FallbackRoutes): RouteTransformer {
  return ({ original, override, children, dupe }) => {
    if (original && !original.index && !getHandleFlag(original, RouteHandleFlag.IGNORE_FALLBACK) && !dupe) {
      if (!children) {
        return original;
      }

      let fallback: RouteObject[];

      if (children.some((route) => isWildcardRoute(route))) {
        fallback = [indexFallback];
      } else if (children.some((route) => isIndexRoute(route) || isOptionalRoute(route))) {
        fallback = [notFoundFallback];
      } else {
        fallback = [notFoundFallback, indexFallback];
      }

      return {
        ...original,
        children: [...children, ...fallback],
      } satisfies NonIndexRouteObject;
    }

    return override as RouteObject | undefined;
  };
}
