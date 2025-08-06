import { createElement } from 'react';
import type { IndexRouteObject, NonIndexRouteObject, RouteObject } from 'react-router';
import { convertComponentNameToTitle } from '../../shared/convertComponentNameToTitle.js';
import type { AgnosticRoute } from '../../types.js';
import { isReactRouteModule, type RouteTransformerOptions } from './utils.js';

/**
 * Transforms a framework-agnostic route into a route object compatible with
 * React Router and merges it with the existing route object if any.
 *
 * @param options - The route transformer options containing the existing route,
 * any overrides, and child routes.
 *
 * @returns A `RouteObject` representing the transformed route, or the original
 * route if no override is provided.
 *
 * @throws If the provided module does not export a React component by default
 * nor a ViewConfig object as "config".
 */
export default function fileRouteTransformer({
  original,
  override,
  children,
}: RouteTransformerOptions<AgnosticRoute>): RouteObject | undefined {
  if (!override) {
    return original;
  }

  const { module, component = module?.default, config = module?.config, path, flowLayout } = override;

  if (module && !isReactRouteModule(module)) {
    throw new Error(
      `The module for the "${path}" section doesn't have the React component exported by default or a ViewConfig object exported as "config"`,
    );
  }

  const element = component ? createElement(component) : undefined;
  const handle = {
    ...config,
    title: config?.title ?? convertComponentNameToTitle(component),
    flowLayout: config?.flowLayout ?? flowLayout,
  };

  if (path === '' && !children) {
    return {
      ...(original as IndexRouteObject),
      element,
      handle,
      index: true,
    } satisfies IndexRouteObject;
  }

  return {
    ...(original as NonIndexRouteObject),
    path: config?.route ?? path,
    element,
    children: children as RouteObject[] | undefined,
    handle,
  } satisfies NonIndexRouteObject;
}
