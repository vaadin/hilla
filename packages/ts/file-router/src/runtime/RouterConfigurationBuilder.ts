import { protectRoutes, type RouteObjectWithAuth } from '@vaadin/hilla-react-auth';
import { type ComponentType, createElement, type ReactElement } from 'react';
import { createBrowserRouter, type RouteObject } from 'react-router-dom';
import { transformTreeSync } from '../shared/transformTree.js';
import type { AgnosticRoute, RouterConfiguration } from '../types.js';
import { toReactRouter } from './toReactRouter.js';

/**
 * Deeply merges two lists of routes. If the specific path is already present,
 * the route is merged, otherwise the new routes are added to the list.
 *
 * @param a - The first list of routes.
 * @param b - The second list of routes.
 */
function mergeRoutes(a: readonly RouteObject[], b: readonly RouteObject[]): RouteObject[] {
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
export class RouterConfigurationBuilder {
  readonly #initializers: Array<(routes: readonly RouteObject[]) => readonly RouteObject[]> = [];
  readonly #finalizers: Array<(routes: readonly RouteObject[]) => readonly RouteObject[]> = [];

  /**
   * Adds the given routes to the current list of routes. All the routes are
   * deeply merged to preserve the path uniqueness.
   *
   * @param routes - A list of routes to add to the current list.
   */
  withReactRoutes(...routes: readonly RouteObject[]): this {
    this.#initializers.push((existingRoutes) => {
      if (!existingRoutes.length) {
        return routes;
      }

      return mergeRoutes(existingRoutes, routes);
    });

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
    this.#initializers.push((existingRoutes) => {
      const reactRoutes = routes.map(toReactRouter);

      if (!existingRoutes.length) {
        return reactRoutes;
      }

      return mergeRoutes(existingRoutes, reactRoutes);
    });
    return this;
  }

  /**
   * Adds the given server route element to each branch of the current list of
   * routes.
   *
   * @param component - The React component to add to each branch of the
   * current list of routes.
   */
  withFallback(component: ComponentType): this {
    this.#finalizers.push((existingRoutes) => {
      const createServerRoute = () => ({ path: '*', element: createElement(component) });

      const newRoutes = existingRoutes.map((route) =>
        transformTreeSync<RouteObject, RouteObject>(
          route,
          (r) => r.children?.values(),
          (r, children) =>
            children
              ? // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
                ({
                  ...r,
                  children: [...children, createServerRoute()],
                } as RouteObject)
              : r,
        ),
      );

      newRoutes.push(createServerRoute());

      return newRoutes;
    });

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
    this.#finalizers.push((existingRoutes) => protectRoutes(existingRoutes as RouteObjectWithAuth[], redirectPath));
    return this;
  }

  /**
   * Builds the router with the current list of routes.
   */
  build(): RouterConfiguration {
    let routes = this.#initializers.reduce<readonly RouteObject[]>((acc, callback) => callback(acc), []);
    routes = this.#finalizers.reduce<readonly RouteObject[]>((acc, callback) => callback(acc), routes);

    return {
      routes,
      router: createBrowserRouter([...routes]),
    };
  }
}
