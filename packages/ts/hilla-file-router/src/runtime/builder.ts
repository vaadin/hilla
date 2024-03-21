import { protectRoutes, type RouteObjectWithAuth } from '@vaadin/hilla-react-auth';
import { createBrowserRouter, type RouteObject } from 'react-router-dom';

function mergeRoutes(a: readonly RouteObject[], b: readonly RouteObject[]): readonly RouteObject[] {
  return [
    ...a.map((route) => {
      const match = b.find(({ path }) => route.path === path);
      return match ? { ...route, ...match, children: mergeRoutes(route.children ?? [], match.children ?? []) } : route;
    }),
    ...b.filter(({ path }) => !a.some((route) => route.path === path)),
  ] as RouteObject[];
}

/**
 * A builder for creating a Vaadin-specific router for React with
 * authentication and server routes support.
 */
export default class RouterBuilder {
  #routes: readonly RouteObject[];

  /**
   * Creates a new router builder with the given routes.
   *
   * @param routes - A list of routes to start with (usually they are frontend
   * routes built with {@link toReactRouter} function.
   */
  constructor(...routes: readonly RouteObject[]) {
    this.#routes = routes;
  }

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
  with(...routes: readonly RouteObject[]): this {
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
    return createBrowserRouter(this.#routes as RouteObject[]);
  }
}
