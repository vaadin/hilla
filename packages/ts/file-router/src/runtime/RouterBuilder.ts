import { protectRoutes, type RouteObjectWithAuth } from '@vaadin/hilla-react-auth';
import type { ReactElement } from 'react';
import { createBrowserRouter, type RouteObject } from 'react-router-dom';
import { transformTreeSync } from '../shared/transformTree.js';
import type { AgnosticRoute } from '../types.js';
import { toReactRouter } from './toReactRouter.js';

/**
 * Deeply merges two lists of routes. If the specific path is already present,
 * the route is merged, otherwise the new routes are added to the list.
 *
 * @param a - The first list of routes.
 * @param b - The second list of routes.
 */
function mergeRoutes(a: RouteObject[], b: RouteObject[]): RouteObject[] {
  return b.reduce(
    (result, route) => {
      const existingRoute = result.find((r) => r.path === route.path);
      if (existingRoute) {
        Object.assign(existingRoute, route);
        existingRoute.children = existingRoute.children
          ? mergeRoutes(existingRoute.children, route.children ?? [])
          : route.children;
      } else {
        result.push(route);
      }
      return result;
    },
    [...a],
  );
}

/**
 * A builder for creating a Vaadin-specific router for React with
 * authentication and server routes support.
 */
export class RouterBuilder {
  #routes: RouteObject[] = [];

  /**
   * Returns the current list of routes.
   */
  get routes(): readonly RouteObject[] {
    return this.#routes;
  }

  /**
   * Adds the given routes to the current list of routes. All the routes are
   * deeply merged to preserve the path uniqueness.
   *
   * @param routes - A list of routes to add to the current list.
   */
  withReactRoutes(...routes: RouteObject[]): this {
    if (!this.#routes.length) {
      this.#routes = routes;
    } else {
      this.#routes = mergeRoutes(this.#routes, routes);
    }

    return this;
  }

  /**
   * Adds the given file routes to the current list of routes. All the routes
   * are transformed to React RouterObjects and deeply merged to preserve the
   * path uniqueness.
   *
   * @param routes - A list of routes to add to the current list.
   */
  withFileRoutes(...routes: readonly AgnosticRoute[]): this {
    const reactRoutes = routes.map(toReactRouter);
    this.withReactRoutes(...reactRoutes);
    return this;
  }

  /**
   * Adds the given server route element to each branch of the current list of
   * routes.
   *
   * @param element - The element to add to each branch of the current list of
   * routes.
   */
  withServerRoutes(element: ReactElement): this {
    const flowRoute: RouteObject = { path: '*', element };

    this.#routes = this.#routes.map((route) =>
      transformTreeSync<RouteObject, RouteObject>(
        route,
        (r) => r.children?.values(),
        (r, children) =>
          // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
          ({
            ...r,
            children: [...children, flowRoute],
          }) as RouteObject,
      ),
    );

    this.#routes.push(flowRoute);

    return this;
  }

  /**
   * Adds the given routes to the current list of routes. All the routes are
   * deeply merged to preserve the path uniqueness.
   *
   * @param routes - A list of routes to add to the current list.
   */
  with(...routes: RouteObject[]): this {
    this.#routes = mergeRoutes(this.#routes, routes);
    return this;
  }

  /**
   * Protects all the routes that require authentication. For more details see
   * {@link @vaadin/hilla-react-auth#protectRoutes} function.
   *
   * @param redirectPath - the path to redirect to if the route is protected
   * and the user is not authenticated.
   */
  protect(redirectPath?: string): this {
    protectRoutes(this.#routes as RouteObjectWithAuth[], redirectPath);
    return this;
  }

  /**
   * Builds the router with the current list of routes.
   */
  build(): ReturnType<typeof createBrowserRouter> {
    return createBrowserRouter(this.#routes);
  }
}
