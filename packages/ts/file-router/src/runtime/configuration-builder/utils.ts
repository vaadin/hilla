import type { RouteObject } from 'react-router';
import type { Module, RouteModule } from '../../types.js';

/**
 * A function type that modifies the current route tree.
 *
 * @param routes - The current route tree to process.
 * @returns An optional readonly array of RouteObject instances after modification
 */
export type RouteTreeModifier = (routes: readonly RouteObject[] | undefined) => readonly RouteObject[] | undefined;

/**
 * A route-like object that can be used in the route tree.
 */
export interface RouteLike {
  path?: string;
  children?: readonly this[];
}

/**
 * Options for transforming a route object within the route configuration builder.
 *
 * @typeParam T - The type of the override object.
 */
export type RouteTransformerOptions<T extends RouteLike> = Readonly<{
  /**
   * Array of child route objects. If omitted, the route is considered leaf.
   */
  children?: readonly RouteObject[];
  /**
   * The original route object to transform.
   */
  original?: RouteObject;
  /**
   * Override object to apply custom transformations.
   */
  override?: T;
  /**
   * Indicates whether the route is a duplicate. Used to handle cases where
   * multiple routes may share the same path.
   */
  dupe?: boolean;
}>;

export type RouteTransformer<T extends RouteLike = RouteLike> = (
  opts: RouteTransformerOptions<T>,
) => RouteObject | undefined;

/**
 * Checks if the given module is a valid React route module.
 *
 * @param module - The JS module to check.
 * @returns True if the module is a valid React route module, false otherwise.
 */
export function isReactRouteModule(module: Module): module is RouteModule {
  return (
    ('default' in module && typeof module.default === 'function') ||
    ('config' in module && typeof module.config === 'object')
  );
}

/**
 * Creates a unique key for a route based on its path and children.
 *
 * @param route - The route object to create a key for.
 * @returns A unique key string for the route.
 */
export function createRouteKey<T extends RouteLike>(route: T): string {
  return `${route.path ?? ''}-${route.children ? 'n' : 'i'}`;
}

/**
 * A set of flags that can be used to control the behavior of routes in the
 * router configuration.
 */
export const RouteHandleFlag = {
  FLOW_LAYOUT: 'flowLayout',
  IGNORE_FALLBACK: 'ignoreFallback',
  SKIP_LAYOUTS: 'skipLayouts',
} as const;
export type RouteHandleFlag = (typeof RouteHandleFlag)[keyof typeof RouteHandleFlag];

/**
 * Retrieves a specific flag from the route's handle object.
 *
 * @param route - The route object to retrieve the flag from.
 * @param flag - The flag to retrieve.
 * @returns The value of the flag if it exists, otherwise undefined.
 */
export function getHandleFlag<T extends RouteHandleFlag>(route: RouteObject, flag: T): boolean | undefined {
  if (typeof route.handle === 'object' && flag in route.handle) {
    return (route.handle as Record<T, boolean>)[flag];
  }

  return undefined;
}
