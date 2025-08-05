import type { RouteObject } from 'react-router';
import type { Module, RouteModule } from '../../types.js';

/**
 * A function type that modifies the current route tree.
 *
 * @param routes - The current route tree to process.
 * @returns An optional readonly array of RouteObject instances after modification
 */
export type RouteTreeModifier = (routes: readonly RouteObject[] | undefined) => readonly RouteObject[] | undefined;

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

export function isReactRouteModule(module: Module): module is RouteModule {
  return (
    ('default' in module && typeof module.default === 'function') ||
    ('config' in module && typeof module.config === 'object')
  );
}

export function createRouteKey<T extends RouteLike>(route: T): string {
  return `${route.path ?? ''}-${route.children ? 'n' : 'i'}`;
}

export const RouteHandleFlag = {
  FLOW_LAYOUT: 'flowLayout',
  IGNORE_FALLBACK: 'ignoreFallback',
  SKIP_LAYOUTS: 'skipLayouts',
} as const;
export type RouteHandleFlag = (typeof RouteHandleFlag)[keyof typeof RouteHandleFlag];

export function getHandleFlag<T extends RouteHandleFlag>(route: RouteObject, flag: T): boolean | undefined {
  if (typeof route.handle === 'object' && flag in route.handle) {
    return (route.handle as Record<T, boolean>)[flag];
  }

  return undefined;
}
