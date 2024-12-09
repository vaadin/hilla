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

export type RouteTransformerOptions<T> = Readonly<{
  children?: RouteList;
  original?: RouteObject;
  overriding?: T;
  dupe: boolean;
}>;

export type RouteTransformer<T> = (opts: RouteTransformerOptions<T>) => RouteObject | undefined;

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
    return this.update(routes, ({ original, overriding: added, children }) => {
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

    this.update(fallbackRoutes, ({ original, overriding: added, children, dupe }) => {
      if (original && !getRouteHandleFlag(original, RouteHandleFlags.IGNORE_FALLBACK) && !dupe) {
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

      const result = transformTree<RouteList, Accumulator<RouteList>>(originalRoutes, null, (routes, next) =>
        // Split a single routes list onto three separate lists:
        // - A list of server routes
        // - A list of client routes
        // - A list of routes which will be moved to either server or client
        // list. It depends on the parent route.
        routes.reduce<Accumulator<WritableRouteList>>(
          (lists, route) => {
            const { server, client, ambivalent } = next(route.children ?? []);

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
              // Even if the route doesn't have the flag, it goes to the server
              // list if any of the children has the flag enabled.
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

            // The route without the flag go to the `default` list. Then it will
            // be moved to either server or client list based on the parent
            // route.
            if (
              flag === undefined &&
              (lists.server.every(({ path }) => path !== route.path) || ambivalent.length > 0)
            ) {
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
              // The server routes are wrapped with the route that has a layout
              // element. It also has the `IGNORE_FALLBACK` flag to remove the
              // fallback route from reach.
              {
                element: createElement(layoutComponent),
                children: result.server as RouteObject[],
                handle: {
                  [RouteHandleFlags.IGNORE_FALLBACK]: true,
                },
              },
            ]
          : []),
        // The client routes are preserved without wrapping.
        ...result.client,
        // The ambivalent routes are considered as client routes.
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
    this.update(undefined, ({ original: route, children }) => {
      const finalRoute = protectRoute(route!, redirectPath);
      finalRoute.children = children as RouteObject[] | undefined;
      return finalRoute;
    });

    return this;
  }

  /**
   * Deeply updates the current list of routes with the given routes merging
   * them in process.
   *
   * @param routes - A list of routes to merge with the current list.
   * @param callback - A callback to transform the routes during the merge.
   */
  update<T extends RouteBase>(routes: undefined, callback: RouteTransformer<undefined>): this;
  update<T extends RouteBase>(routes: readonly T[], callback?: RouteTransformer<T>): this;
  update<T extends RouteBase>(
    routes: readonly T[] | undefined,
    callback: RouteTransformer<T | undefined> = ({ original, overriding, children }) =>
      ({
        ...original,
        ...overriding,
        children,
      }) as RouteObject,
  ): this {
    this.#modifiers.push((existingRoutes) =>
      // Going through the existing and added list of routes.
      transformTree<readonly [RouteList | undefined, readonly T[] | undefined], RouteList | undefined>(
        [existingRoutes, routes],
        null,
        ([original, added], next) => {
          if (original && added) {
            // If we have both original and added routes, we have to merge them.
            const final: Array<RouteObject | undefined> = [];
            const paths = new Set([...original.map(({ path }) => path), ...added.map(({ path }) => path)]);

            for (const path of paths) {
              // We can have multiple routes with the same path, so we have to
              // consider all of them.
              const originalRoutes = original.filter((r) => r.path === path);
              // We can have only one route with the same path in the added list.
              const addedRoutes = added.filter((r) => r.path === path);

              if (addedRoutes.length > 1) {
                throw new Error('Adding multiple routes with the same path is not allowed');
              }

              const addedRoute = addedRoutes[0] as T | undefined;

              if (originalRoutes.length > 0 && addedRoute) {
                // In case we have both original and added routes, we run
                // the callback for each original route in pair with the added
                // route. To make the difference, we flag all the routes except
                // the last one as `dupe`.
                //
                // Why the last one is not `dupe`? According to the
                // `react-router` logic, the last route is the fallback for all
                // routes with the same path. So, if we apply callback to it,
                // we implicitly apply it to all other routes with the same
                // path.
                //
                // In case this logic doesn't work, the user can apply the
                // callback without considering the `dupe` flag.
                for (let i = 0; i < originalRoutes.length; i++) {
                  final.push(
                    callback({
                      original: originalRoutes[i],
                      overriding: addedRoute,
                      children: next([originalRoutes[i].children, addedRoute.children]),
                      dupe: i < originalRoutes.length - 1,
                    }) ?? originalRoutes[i],
                  );
                }
              } else if (originalRoutes.length > 0) {
                // In case we don't have the added route with the path being
                // processed, we run the callback for each original route.
                for (let i = 0; i < originalRoutes.length; i++) {
                  final.push(
                    callback({
                      original: originalRoutes[i],
                      children: next([originalRoutes[i].children, undefined]),
                      dupe: i < originalRoutes.length - 1,
                    }) ?? originalRoutes[i],
                  );
                }
              } else {
                // In case we don't have the original route with the path being
                // processed, we run the callback for only the added route.
                const result = callback({
                  overriding: addedRoute,
                  children: next([undefined, addedRoute!.children]),
                  dupe: false,
                });

                if (result) {
                  final.push(result);
                }
              }
            }

            return final.filter((r) => r != null);
          } else if (original) {
            // If we have only original routes, we run the callback for each
            // original route.
            return original
              .map((route) =>
                callback({
                  original: route,
                  children: next([route.children, undefined]),
                  dupe: false,
                }),
              )
              .filter((r) => r != null);
          } else if (added) {
            // If we have only added routes, we run the callback for each added
            // route.
            return added
              .map((route) =>
                callback({
                  overriding: route,
                  children: next([undefined, route.children]),
                  dupe: false,
                }),
              )
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

      const result = transformTree<RouteList, Accumulator<RouteList>>(originalRoutes, null, (routes, next) =>
        // Split a single routes list onto two separate lists.
        routes.reduce<Accumulator<WritableRouteList>>(
          (lists, route) => {
            // If the route has `skipLayout` flag, it goes to the `skipped` list.
            if (getRouteHandleFlag(route, RouteHandleFlags.SKIP_LAYOUTS)) {
              lists.skipped.push(route);
              return lists;
            }

            // If the route is leaf, it goes to the `regular` list.
            if (!route.children?.length) {
              lists.regular.push(route);
              return lists;
            }

            // As of children, we have to split them into two lists as well.
            const { skipped, regular } = next(route.children ?? []);

            // If we have `skipped` list of children, we have to remove the
            // `element` property of the router to prevent the layout from
            // rendering. Then, we add the current route to the `skipped` list.
            if (skipped.length > 0) {
              const { element, ...rest } = route;

              lists.skipped.push({
                ...rest,
                children: skipped,
              } as RouteObject);
            }

            // In case of `regular` children, we just add the current route to
            // the `regular` list if there are any children.
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

      // We don't need a fallback for the skipped routes, so we have to wrap
      // them with the route with the `IGNORE_FALLBACK` flag.
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
