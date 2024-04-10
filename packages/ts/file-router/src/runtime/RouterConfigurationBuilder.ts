/* eslint-disable @typescript-eslint/consistent-type-assertions */
import { protectRoute } from '@vaadin/hilla-react-auth';
import { type ComponentType, createElement } from 'react';
import { createBrowserRouter, type RouteObject } from 'react-router-dom';
import { convertComponentNameToTitle } from '../shared/convertComponentNameToTitle.js';
import { transformTree2 } from '../shared/transformTree2.js';
import type { AgnosticRoute, Module, RouteModule, RouterConfiguration, ViewConfig } from '../types.js';

interface RouteBase {
  path?: string;
  children?: readonly this[];
}

type RoutesModifier = (routes: readonly RouteObject[]) => readonly RouteObject[];

function isReactRouteModule(module?: Module): module is RouteModule<ComponentType> | undefined {
  return module ? 'default' in module && typeof module.default === 'function' : true;
}

type RouteTransformer<T> = (
  original: RouteObject | undefined,
  overriding: T,
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
  withReactRoutes(...routes: readonly RouteObject[]): this {
    return this.update(routes);
  }

  /**
   * Adds the given file routes to the current list of routes. All the routes
   * are transformed to React RouterObjects and deeply merged to preserve the
   * path uniqueness.
   *
   * @param routes - A list of routes to add to the current list.
   */
  withFileRoutes(...routes: readonly AgnosticRoute[]): this {
    return this.update(routes, (original, { module, path }, children) => {
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
    const serverRoute = { path: '*', element: createElement(component), handle: config };

    this.update(undefined, (original, _, children) =>
      children
        ? ({
            ...original!,
            children: [...children, serverRoute],
          } as RouteObject)
        : original!,
    );

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
    this.#modifiers.push(
      (existingRoutes) =>
        transformTree2<
          [readonly RouteObject[] | undefined, readonly T[] | undefined],
          readonly RouteObject[] | undefined
        >([existingRoutes, routes], ([original, incoming], next) => {
          if (original && incoming) {
            const final = new Map(original.map((route) => [route.path, route]));

            for (const route of incoming) {
              const existingRoute = final.get(route.path);
              if (existingRoute) {
                final.set(
                  existingRoute.path,
                  callback(existingRoute, route, next(existingRoute.children, route.children)),
                );
              } else {
                const newRoute = callback(undefined, route, next(undefined, route.children));
                final.set(newRoute.path, newRoute);
              }
            }

            return [...final.values()];
          } else if (original) {
            return original;
          } else if (incoming) {
            return incoming.map((route) => callback(undefined, route, next(undefined, route.children)));
          }

          return undefined;
        }) ?? [],
    );
    return this;
  }

  /**
   * Builds the router with the current list of routes.
   */
  build(): RouterConfiguration {
    const routes = this.#modifiers.reduce<readonly RouteObject[]>((acc, mod) => mod(acc), []);

    return {
      routes,
      router: createBrowserRouter([...routes]),
    };
  }
}
