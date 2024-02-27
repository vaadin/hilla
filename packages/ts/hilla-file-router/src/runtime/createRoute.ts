/* eslint-disable no-param-reassign */
import type { AgnosticRoute, RouteModule } from '../types.js';

/**
 * Create a single framework-agnostic route object. Later, it can be transformed into a framework-specific route object,
 * e.g., the one used by React Router.
 *
 * @param path - A route path segment.
 * @param children - An array of child routes.
 *
 * @returns A framework-agnostic route object.
 */
export function createRoute<C = unknown>(path: string, children?: ReadonlyArray<AgnosticRoute<C>>): AgnosticRoute<C>;
export function createRoute<C = unknown>(
  path: string,
  module: RouteModule<C>,
  children?: ReadonlyArray<AgnosticRoute<C>>,
): AgnosticRoute<C>;
export function createRoute<C = unknown>(
  path: string,
  moduleOrChildren?: ReadonlyArray<AgnosticRoute<C>> | RouteModule<C>,
  children?: ReadonlyArray<AgnosticRoute<C>>,
): AgnosticRoute<C> {
  let module: RouteModule<C> | undefined;
  if (Array.isArray(moduleOrChildren)) {
    children = moduleOrChildren;
  } else {
    module = moduleOrChildren as RouteModule<C> | undefined;
  }

  return {
    path: module?.config?.route ?? path,
    module,
    children,
  };
}
