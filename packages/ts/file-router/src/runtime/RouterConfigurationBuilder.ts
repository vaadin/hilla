/* eslint-disable @typescript-eslint/consistent-type-assertions */
import { protectRoute } from '@vaadin/hilla-react-auth';
import { type ComponentType, createElement } from 'react';
import {
  createBrowserRouter,
  type IndexRouteObject,
  type NonIndexRouteObject,
  type RouteObject,
} from 'react-router-dom';
import { convertComponentNameToTitle } from '../shared/convertComponentNameToTitle.js';
import { transformTree } from '../shared/transformTree.js';
import type {
  AgnosticRoute,
  Module,
  RouteModule,
  RouterBuildOptions,
  RouterConfiguration,
  ViewConfig,
} from '../types.js';

interface RouteBase {
  path?: string;
  children?: readonly this[];
}

function isReactRouteModule(module?: Module): module is RouteModule<ComponentType> | undefined {
  if (!module) {
    return true;
  }

  return (
    ('default' in module && typeof module.default === 'function') ||
    ('config' in module && typeof module.config === 'object')
  );
}

export type RouteList = readonly RouteObject[];
export type WritableRouteList = RouteObject[];

export type RouteTransformer<T> = (
  original: RouteObject | undefined,
  overriding: T | undefined,
  children?: RouteList,
) => RouteObject | undefined;

export type RouteListSplittingRule = (route: RouteObject) => boolean;

type RoutesModifier = (routes: RouteList | undefined) => RouteList | undefined;

function createRouteEntry<T extends RouteBase>(route: T): readonly [key: string, value: T] {
  return [`${route.path ?? ''}-${route.children ? 'n' : 'i'}`, route];
}

enum RouteHandleFlags {
  FLOW_LAYOUT = 'flowLayout',
  IGNORE_FALLBACK = 'ignoreFallback',
  SKIP_LAYOUTS = 'skipLayouts',
}

function getRouteHandleFlag<T extends RouteHandleFlags>(route: RouteObject, flag: T): boolean | undefined {
  if (typeof route.handle === 'object' && flag in route.handle) {
    return (route.handle as Record<T, boolean>)[flag];
  }

  return undefined;
}

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
  withReactRoutes(routes: RouteList): this {
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
        const { module, path, flowLayout } = added;
        if (!isReactRouteModule(module)) {
          throw new Error(
            `The module for the "${path}" section doesn't have the React component exported by default or a ViewConfig object exported as "config"`,
          );
        }

        const element = module?.default ? createElement(module.default) : undefined;
        const handle = {
          ...module?.config,
          title: module?.config?.title ?? convertComponentNameToTitle(module?.default),
          flowLayout: module?.config?.flowLayout ?? flowLayout,
        };

        if (path === '' && !children) {
          return {
            ...original,
            element,
            handle,
            index: true,
          } as IndexRouteObject;
        }

        return {
          ...original,
          path: module?.config?.route ?? path,
          element,
          children,
          handle,
        } as NonIndexRouteObject;
      }

      return original;
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
    this.withLayout(component);

    // Fallback adds two routes, so that the index (empty path) has a fallback too
    const fallbackRoutes: RouteList = [
      { path: '*', element: createElement(component), handle: config },
      { index: true, element: createElement(component), handle: config },
    ];

    this.update(fallbackRoutes, (original, added, children) => {
      if (original && !getRouteHandleFlag(original, RouteHandleFlags.IGNORE_FALLBACK)) {
        if (!children) {
          return original;
        }

        const _fallback = [...fallbackRoutes];

        if (children.some(({ path }) => path === '*')) {
          _fallback.shift();
        }

        if (children.some(({ index: i, path }) => i ?? path?.includes('?'))) {
          _fallback.pop();
        }

        return {
          ...original,
          children: [...children, ..._fallback],
        } as RouteObject;
      }

      return added!;
    });

    return this;
  }

  /**
   * Adds the layoutComponent as the parent layout to views with the flowLayouts ViewConfiguration set.
   *
   * @param layoutComponent - layout component to use, usually Flow
   */
  withLayout(layoutComponent: ComponentType): this {
    this.#modifiers.push((originalRoutes) => {
      if (!originalRoutes) {
        return originalRoutes;
      }

      type Accumulator<T extends RouteList> = Readonly<{
        server: T;
        client: T;
        ambivalent: T;
      }>;

      const result = transformTree<RouteList, Accumulator<RouteList>>(originalRoutes, (routes, next) =>
        // Split a single routes list onto three separate lists:
        // - A list of server routes
        // - A list of client routes
        // - A list of routes which will be moved to either server or client
        // list. It depends on the parent route.
        routes.reduce<Accumulator<WritableRouteList>>(
          (lists, route) => {
            const { server, client, ambivalent } = next(...(route.children ?? []));

            const flag = getRouteHandleFlag(route, RouteHandleFlags.FLOW_LAYOUT);

            // If the route has `flowLayout` flag explicitly enabled, it goes to
            // the server list. The children are also affected by the flag
            // unless they have it explicitly disabled.
            if (flag === true) {
              lists.server.push({
                ...route,
                children: server.length + ambivalent.length > 0 ? [...server, ...ambivalent] : undefined,
              } as RouteObject);
            } else if (server.length > 0) {
              lists.server.push({
                ...route,
                children: server,
              } as RouteObject);
            }

            // If the route has `flowLayout` flag explicitly disabled, it goes
            // to the client list. The route children are not affected by the
            // flag.
            if (flag === false || client.length > 0) {
              lists.client.push({
                ...route,
                children: client.length > 0 ? client : undefined,
              } as RouteObject);
            }

            if (flag === undefined && lists.server.every(({ path }) => path !== route.path)) {
              // The route without the flag go to the `default` list. Then it will
              // be moved to either server or client list based on the parent
              // route.
              lists.ambivalent.push({
                ...route,
                children: ambivalent.length > 0 ? ambivalent : undefined,
              } as RouteObject);
            }

            return lists;
          },
          { server: [], client: [], ambivalent: [] },
        ),
      );

      return [
        ...(result.server.length
          ? [
              // The server subtree is wrapped with the server layout component,
              // which applies the top-level server layout to all matches.
              {
                element: createElement(layoutComponent),
                children: result.server as RouteObject[],
                handle: {
                  [RouteHandleFlags.IGNORE_FALLBACK]: true,
                },
              },
            ]
          : []),
        // The client route subtree is preserved without wrapping.
        ...result.client,
        // The default routes are considered as client routes.
        ...result.ambivalent,
      ];
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
      transformTree<[RouteList | undefined, readonly T[] | undefined], RouteList | undefined>(
        [existingRoutes, routes],
        ([original, added], next) => {
          if (original && added) {
            const originalMap = new Map(original.map((route) => createRouteEntry(route)));
            const addedMap = new Map(added.map((route) => createRouteEntry(route)));

            const paths = new Set([...originalMap.keys(), ...addedMap.keys()]);

            for (const path of paths) {
              const originalRoute = originalMap.get(path);
              const addedRoute = addedMap.get(path);

              let route: RouteObject | undefined;
              if (originalRoute && addedRoute) {
                route = callback(originalRoute, addedRoute, next(originalRoute.children, addedRoute.children));
              } else if (originalRoute) {
                route = callback(originalRoute, undefined, next(originalRoute.children, undefined));
              } else {
                route = callback(undefined, addedRoute, next(undefined, addedRoute!.children));
              }

              if (route) {
                originalMap.set(path, route);
              }
            }

            return [...originalMap.values()];
          } else if (original) {
            return original
              .map((route) => callback(route, undefined, next(route.children, undefined)))
              .filter((r) => r != null);
          } else if (added) {
            return added
              .map((route) => callback(undefined, route, next(undefined, route.children)))
              .filter((r) => r != null);
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
  build(options?: RouterBuildOptions): RouterConfiguration {
    this.#withLayoutSkipping();
    const routes = this.#modifiers.reduce<RouteList | undefined>((acc, mod) => mod(acc) ?? acc, undefined) ?? [];

    return {
      routes,
      router: createBrowserRouter([...routes], {
        basename: new URL(document.baseURI).pathname,
        future: {
          // eslint-disable-next-line camelcase
          v7_fetcherPersist: true,
          // eslint-disable-next-line camelcase
          v7_normalizeFormMethod: true,
          // eslint-disable-next-line camelcase
          v7_partialHydration: true,
          // eslint-disable-next-line camelcase
          v7_relativeSplatPath: true,
          // eslint-disable-next-line camelcase
          v7_skipActionErrorRevalidation: true,
        },
        ...options,
      }),
    };
  }

  #withLayoutSkipping(): this {
    this.#modifiers.push((originalRoutes) => {
      if (!originalRoutes) {
        return originalRoutes;
      }

      type Accumulator<T extends RouteList> = Readonly<{
        skipped: T;
        regular: T;
      }>;

      const result = transformTree<RouteList, Accumulator<RouteList>>(originalRoutes, (routes, next) =>
        // Split a single routes list onto two separate lists.
        routes.reduce<Accumulator<WritableRouteList>>(
          (lists, route) => {
            // If the route has `skipLayout` flag, it goes to the `skipped` list.
            if (getRouteHandleFlag(route, RouteHandleFlags.SKIP_LAYOUTS)) {
              lists.skipped.push(route);
              return lists;
            }

            if (!route.children?.length) {
              lists.regular.push(route);
              return lists;
            }

            const { skipped, regular } = next(...(route.children ?? []));

            if (skipped.length > 0) {
              const { element, ...rest } = route;

              lists.skipped.push({
                ...rest,
                children: skipped,
              } as RouteObject);
            }

            if (regular.length > 0) {
              lists.regular.push({
                ...route,
                children: regular,
              } as RouteObject);
            }

            return lists;
          },
          { skipped: [], regular: [] },
        ),
      );

      return [
        ...(result.skipped.length
          ? [
              {
                children: result.skipped as RouteObject[],
                handle: {
                  [RouteHandleFlags.IGNORE_FALLBACK]: true,
                },
              },
            ]
          : []),
        ...result.regular,
      ];
    });

    return this;
  }
}
