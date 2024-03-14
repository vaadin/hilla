import type { AgnosticRoute, Module } from '../types.js';

/**
 * Create a single framework-agnostic route object. Later, it can be transformed into a framework-specific route object,
 * e.g., the one used by React Router.
 *
 * @param path - A route path segment.
 * @param children - An array of child routes.
 *
 * @returns A framework-agnostic route object.
 */
export function createRoute(path: string, children?: readonly AgnosticRoute[]): AgnosticRoute;
export function createRoute(path: string, module: Module, children?: readonly AgnosticRoute[]): AgnosticRoute;
export function createRoute(
  path: string,
  moduleOrChildren?: Module | readonly AgnosticRoute[],
  children?: readonly AgnosticRoute[],
): AgnosticRoute {
  let module: Module | undefined;
  if (Array.isArray(moduleOrChildren)) {
    // eslint-disable-next-line no-param-reassign
    children = moduleOrChildren;
  } else {
    module = moduleOrChildren as Module | undefined;
  }

  return {
    path,
    module,
    children,
  };
}
