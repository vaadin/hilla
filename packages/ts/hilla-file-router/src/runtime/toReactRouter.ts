import { type ComponentType, createElement } from 'react';
import type { RouteObject as ReactRouteObject } from 'react-router-dom';
import { convertComponentNameToTitle } from '../shared/convertComponentNameToTitle.js';
import { transformTreeSync } from '../shared/transformTree.js';
import type { AgnosticRoute, Module, RouteModule } from '../types.js';

function isReactRouteModule(module?: Module): module is RouteModule<ComponentType> | undefined {
  return module ? 'default' in module && typeof module.default === 'function' : true;
}

/**
 * Transforms framework-agnostic route tree into a format that can be used by React Router.
 *
 * @param routes - Generated routes
 *
 * @returns The React Router tree.
 */
export function toReactRouter(routes: AgnosticRoute): ReactRouteObject {
  return transformTreeSync(
    routes,
    (route) => route.children?.values(),
    ({ path, module }, children) => {
      if (!isReactRouteModule(module)) {
        throw new Error(`The module for the "${path}" section doesn't have the React component exported by default`);
      }

      const title = module?.config?.title ?? convertComponentNameToTitle(module?.default);

      return {
        path: module?.config?.route ?? path,
        element: module?.default ? createElement(module.default) : undefined,
        children: children.length > 0 ? (children as ReactRouteObject[]) : undefined,
        handle: {
          ...module?.config,
          title,
        },
      } satisfies ReactRouteObject;
    },
  );
}
