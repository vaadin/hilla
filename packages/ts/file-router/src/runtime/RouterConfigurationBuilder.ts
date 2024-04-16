/* eslint-disable @typescript-eslint/consistent-type-assertions */
import { protectRoute } from '@vaadin/hilla-react-auth';
import { type ComponentType, createElement } from 'react';
import { createBrowserRouter, type RouteObject } from 'react-router-dom';
import { convertComponentNameToTitle } from '../shared/convertComponentNameToTitle.js';
import { transformTree } from '../shared/transformTree.js';
import type { AgnosticRoute, Module, RouteModule, RouterConfiguration, ViewConfig } from '../types.js';

interface RouteBase {
  path?: string;
  children?: readonly this[];
}

type RoutesModifier = (routes: readonly RouteObject[] | undefined) => readonly RouteObject[] | undefined;

function isReactRouteModule(module?: Module): module is RouteModule<ComponentType> | undefined {
  return module ? 'default' in module && typeof module.default === 'function' : true;
}

type RouteTransformer<T> = (
  original: RouteObject | undefined,
  overriding: T | undefined,
  children?: readonly RouteObject[],
) => RouteObject;

/**
 * A builder for creating a Vaadin-specific router for React with
 * authentication and server routes support.
 */
export class RouterConfigurationBuilder {
  readonly #modifiers: RoutesModifier[] = [];

  /**
   * Adds the given routes to the current list of routes. All the routes are
   * deeply merged to preserve the path uniqueness.
   *
   * @param routes - A list of routes to add to the current list.
   */
  withReactRoutes(routes: readonly RouteObject[]): this {
    return this.update(routes);
  }

  /**
   * Adds the given file routes to the current list of routes. All the routes
   * are transformed to React RouterObjects and deeply merged to preserve the
   * path uniqueness.
   *
   * @param routes - A list of routes to add to the current list.
   */
  withFileRoutes(routes: readonly AgnosticRoute[]): this {
    return this.update(routes, (original, added, children) => {
      if (added) {
        const { module, path } = added;
        if (!isReactRouteModule(module)) {
          throw new Error(`The module for the "${path}" section doesn't have the React component exported by default`);
        }

        const title = module?.config?.title ?? convertComponentNameToTitle(module?.default);

        return {
          ...original,
          path: module?.config?.route ?? path,
          element: module?.default ? createElement(module.default) : undefined,
          children: children as RouteObject[] | undefined,
          handle: {
            ...module?.config,
            title,
          },
        } as RouteObject;
      }

      return original!;
    });
  }

  /**
   * Adds the given server route element to each branch of the current list of
   * routes.
   *
   * @param component - The React component to add to each branch of the
   * current list of routes.
   * @param config - An optional configuration that will be applied to
   * each fallback component.
   */
  withFallback(component: ComponentType, config?: ViewConfig): this {
    // Fallback adds two routes, so that the index (empty path) has a fallback too
    const fallbackRoutes = [
      { path: '*', element: createElement(component), handle: config },
      { index: true, element: createElement(component), handle: config },
    ];

    this.update(fallbackRoutes, (original, added, children) => {
      if (original) {
        return children
          ? ({
              ...original,
              children: [...children, ...fallbackRoutes],
            } as RouteObject)
          : original;
      }

      return added!;
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
    this.update(undefined, (route, _, children) => {
      const finalRoute = protectRoute(route!, redirectPath);
      finalRoute.children = children as RouteObject[] | undefined;
      return finalRoute;
    });

    return this;
  }

  update<T extends RouteBase>(routes: undefined, callback: RouteTransformer<undefined>): this;
  update<T extends RouteBase>(routes: readonly T[], callback?: RouteTransformer<T>): this;
  update<T extends RouteBase>(
    routes: readonly T[] | undefined,
    callback: RouteTransformer<T | undefined> = (original, overriding, children) =>
      ({
        ...original,
        ...overriding,
        children,
      }) as RouteObject,
  ): this {
    this.#modifiers.push((existingRoutes) =>
      transformTree<[readonly RouteObject[] | undefined, readonly T[] | undefined], readonly RouteObject[] | undefined>(
        [existingRoutes, routes],
        ([original, added], next) => {
          if (original && added) {
            const originalMap = new Map(original.map((route) => [route.path, route]));
            const addedMap = new Map(added.map((route) => [route.path, route]));

            const paths = new Set([...originalMap.keys(), ...addedMap.keys()]);

            for (const path of paths) {
              const originalRoute = originalMap.get(path);
              const addedRoute = addedMap.get(path);

              let route: RouteObject;
              if (originalRoute && addedRoute) {
                route = callback(originalRoute, addedRoute, next(originalRoute.children, addedRoute.children));
              } else if (originalRoute) {
                route = callback(originalRoute, undefined, next(originalRoute.children, undefined));
              } else {
                route = callback(undefined, addedRoute, next(undefined, addedRoute!.children));
              }

              originalMap.set(path, route);
            }

            return [...originalMap.values()];
          } else if (original) {
            return original.map((route) => callback(route, undefined, next(route.children, undefined)));
          } else if (added) {
            return added.map((route) => callback(undefined, route, next(undefined, route.children)));
          }

          return undefined;
        },
      ),
    );
    return this;
  }

  /**
   * Builds the router with the current list of routes.
   */
  build(): RouterConfiguration {
    const routes =
      this.#modifiers.reduce<readonly RouteObject[] | undefined>((acc, mod) => mod(acc) ?? acc, undefined) ?? [];

    return {
      routes,
      router: createBrowserRouter([...routes]),
    };
  }
}
