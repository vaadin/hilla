/* eslint-disable @typescript-eslint/no-use-before-define */
import type { RouteObject } from 'react-router';
import { transformTree, type TreeTransformerNextCallback } from '../../shared/transformTree.js';
import { createRouteKey, type RouteLike, type RouteTransformer, type RouteTransformerOptions } from './utils.js';

type RoutesOverridesPair<T> = readonly [
  routes: readonly RouteObject[] | undefined,
  overrides: readonly T[] | undefined,
];

function defaultRouteTransformer<T extends RouteLike>({
  original,
  override,
  children,
}: RouteTransformerOptions<T>): RouteObject {
  // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
  return {
    ...original,
    ...override,
    children,
  } as RouteObject;
}

type TransformerContext<T extends RouteLike> = Readonly<{
  transformer: RouteTransformer<T>;
  next: TreeTransformerNextCallback<RoutesOverridesPair<T>, readonly RouteObject[] | undefined>;
}>;

/**
 * Updates a route tree by merging, transforming, or replacing route objects.
 *
 * This function takes an existing route tree and a new tree of routes, then
 * applies a transformer recursively to produce an updated route tree. The
 * update strategy depends on the presence of the original and added trees:
 * - If both `existingRoutes` and `tree` are provided, they are recursively
 * merged them using the provided transformer.
 * - If only `existingRoutes` is provided, only the original routes are
 * transformed.
 * - If only `tree` is provided, it is transformed and used as a new route tree.
 *
 * @typeParam T - The type of the route-like objects in the tree.
 *
 * @param existingRoutes - The current route tree.
 * @param tree - The new tree of route-like objects to merge recursively.
 * @param transformer - A function to transform route objects.
 *
 * @returns The updated route tree as an array of `RouteObject`, or `undefined`
 * if no routes are present.
 */
export function mergeRouteTrees<T extends RouteLike>(
  existingRoutes: readonly RouteObject[] | undefined,
  tree: readonly T[] | undefined,
  transformer: RouteTransformer<T> = defaultRouteTransformer,
): readonly RouteObject[] | undefined {
  return transformTree([existingRoutes, tree] as const, null, ([original, added], next) => {
    if (original && added) {
      return mergeBothTrees(original, added, { transformer, next });
    }

    if (original) {
      return transformOriginalRoutesOnly(original, { transformer, next });
    }

    if (added) {
      return added.map((route) => transformOverrideOnly(route, { transformer, next })).filter((r) => r != null);
    }

    return undefined;
  });
}

/**
 * Merges two route trees into a single one, applying transformations and
 * overrides as specified by the provided context.
 *
 * For each unique path, this function:
 * - Applies the override to all matching original routes if both exist.
 * - Transforms original routes if no override exists.
 * - Transforms override and adds it as a new route if no original route exists.
 *
 * @typeParam T - The type of the override route, extending `RouteLike`.
 *
 * @param originals - The original tree of route objects.
 * @param overrides - The tree of override route objects.
 * @param ctx - The context used for transforming and applying overrides.
 *
 * @returns A new tree of merged and transformed route objects.
 * @throws If multiple overrides with the same route key are found.
 */
function mergeBothTrees<T extends RouteLike>(
  originals: readonly RouteObject[],
  overrides: readonly T[],
  ctx: TransformerContext<T>,
): readonly RouteObject[] {
  const pathKeys = new Set([...originals.map((r) => createRouteKey(r)), ...overrides.map((r) => createRouteKey(r))]);

  return Array.from(pathKeys).reduce<RouteObject[]>((acc, pathKey) => {
    // For the original routes, we can have multiple routes with the same path.
    const originalRoutes = originals.filter((r) => createRouteKey(r) === pathKey);

    // However, for the added routes, we can only take the first one.
    const _overrides = overrides.filter((r) => createRouteKey(r) === pathKey);

    if (_overrides.length > 1) {
      throw new Error('Adding multiple routes with the same path is not allowed');
    }

    const [override] = _overrides;

    if (originalRoutes.length > 0 && override) {
      applyOverrideForMultipleRoutesWithSamePath(originalRoutes, override, ctx).forEach((route) => acc.push(route));
    } else if (originalRoutes.length > 0) {
      transformOriginalRoutesWithSamePath(originalRoutes, ctx).forEach((route) => acc.push(route));
    } else if (override) {
      const route = transformOverrideOnly(override, ctx);

      if (route) {
        acc.push(route);
      }
    }

    return acc;
  }, []);
}

/**
 * In this case, we have multiple original routes and one override per path. To
 * merge everything together, we apply the transformer to each original route in
 * conjunction with the override.
 *
 * @remarks To disambiguate the routes, we pass the `dupe` flag to the
 * transformer for each original route except the last one, which is considered
 * the "main" route. It follows the React Router logic, where the last route
 * serves as the fallback for all previous routes with the same path.
 *
 * @typeParam T - The type of the route-like to apply.
 *
 * @param routes - Original routes to update (they all have the same path).
 * @param override - The override route-like to apply.
 * @param transformer - The transformer function to apply to each route/override
 * pair.
 * @param next - The next callback to call with the children of the routes.
 *
 * @returns The updated routes with the override applied.
 */
function applyOverrideForMultipleRoutesWithSamePath<T extends RouteLike>(
  routes: readonly RouteObject[],
  override: T,
  { transformer, next }: TransformerContext<T>,
): readonly RouteObject[] {
  return routes.map(
    (route, index) =>
      transformer({
        original: route,
        override,
        children: next([route.children, override.children]),
        dupe: index < routes.length - 1,
      }) ?? route,
  );
}

function createOriginalRoutesOnlyTransformer(shouldHandleDuplicates: boolean) {
  return <T extends RouteLike>(
    routes: readonly RouteObject[],
    { transformer, next }: TransformerContext<T>,
  ): readonly RouteObject[] =>
    routes.map(
      (route, index) =>
        transformer({
          original: route,
          children: next([route.children, undefined]),
          dupe: shouldHandleDuplicates && index < routes.length - 1,
        }) ?? route,
    );
}

/**
 * In this case, we have multiple original routes and one override per path. To
 * merge everything together, we apply the transformer to each original route in
 * conjunction with the override.
 *
 * @remarks As in {@link applyOverrideForMultipleRoutesWithSamePath}, here the
 * `dupe` flag is also used to indicate that the transforming route is not the
 * "main" route (the last one in the list).
 *
 * @typeParam T - The type of the route-like to apply.
 *
 * @param routes - Original routes to update (they all have the same path).
 * @param transformer - The transformer function to apply to each route.
 * @param next - The next callback to call with the children of the routes.
 *
 * @returns The updated routes with the transformer applied.
 */
const transformOriginalRoutesWithSamePath = createOriginalRoutesOnlyTransformer(true);

/**
 * In this case, we don't have override tree at all, so we simply transform the
 * original routes only.
 *
 * @typeParam T - The type of the route-like to apply.
 *
 * @param routes - Original routes to update (they all have the same path).
 * @param transformer - The transformer function to apply to each route.
 * @param next - The next callback to call with the children of the routes.
 *
 * @returns The updated routes with the transformer applied.
 */
const transformOriginalRoutesOnly = createOriginalRoutesOnlyTransformer(false);

/**
 * In this case, we have no original routes, so we simply apply the
 * transformer to the override tree, creating a new route object.
 *
 * This function adds a completely new route to the tree.
 *
 * @param override - The override route-like to apply.
 * @param transformer - The transformer function to apply to the override.
 * @param next - The next callback to call with the children of the override.
 *
 * @returns The new route created from the override.
 */
function transformOverrideOnly<T extends RouteLike>(
  override: T,
  { transformer, next }: TransformerContext<T>,
): RouteObject | undefined {
  return transformer({
    original: undefined,
    override,
    children: next([undefined, override.children]),
  });
}
