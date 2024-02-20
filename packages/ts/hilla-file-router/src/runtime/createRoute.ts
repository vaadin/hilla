/* eslint-disable no-param-reassign */
import type { AgnosticRoute, RouteModule } from '../types.js';

function createRoute<C = unknown>(path: string, children?: ReadonlyArray<AgnosticRoute<C>>): AgnosticRoute<C>;
function createRoute<C = unknown>(
  path: string,
  module: RouteModule<C>,
  children?: ReadonlyArray<AgnosticRoute<C>>,
): AgnosticRoute<C>;
function createRoute<C = unknown>(
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

export { createRoute as r };
