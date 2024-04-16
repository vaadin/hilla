import { type ComponentType, createElement } from 'react';
import type { IndexRouteObject, NonIndexRouteObject, RouteObject as ReactRouteObject } from 'react-router-dom';
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
    ({ path: filePath, module }, children) => {
      if (!isReactRouteModule(module)) {
        throw new Error(
          `The module for the "${filePath}" section doesn't have the React component exported by default`,
        );
      }

      const path = module?.config?.route ?? filePath;
      const title = module?.config?.title ?? convertComponentNameToTitle(module?.default);
      const element = module?.default ? createElement(module.default) : undefined;

      const routeData = {
        element,
        handle: {
          ...module?.config,
          title,
        },
      };

      // Routes with path "" and no children are directory index routes. To
      // make them match in their parent layout, ReactRouter requires declaring
      // { index: true } instead of the path.
      const hasChildren = children && children.length > 0;
      const isIndex = !hasChildren && path === '';
      return isIndex
        ? ({ index: true, ...routeData } satisfies IndexRouteObject)
        : ({
            path,
            ...routeData,
            children: children as ReactRouteObject[] | undefined,
          } satisfies NonIndexRouteObject);
    },
  );
}
