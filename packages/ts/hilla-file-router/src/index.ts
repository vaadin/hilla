import { type ComponentType, createElement } from 'react';
import type { NonIndexRouteObject, RouteObject } from 'react-router';
import { type AgnosticRoute, transformRoute } from './utils.js';

export function toReact(routes: AgnosticRoute<ComponentType, RouteObject>): RouteObject {
  return transformRoute(
    routes,
    (route) => route.children?.values(),
    ({ path, component, meta }, children) =>
      ({
        ...meta,
        path,
        element: component ? createElement(component) : undefined,
        children: children.length > 0 ? (children as RouteObject[]) : undefined,
      }) as RouteObject,
  );
}
