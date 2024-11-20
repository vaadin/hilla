/* eslint-disable @typescript-eslint/consistent-type-assertions */
import { protectRoute } from '@vaadin/hilla-react-auth';
import { type ComponentType, createElement } from 'react';
import {
  createBrowserRouter,
  type IndexRouteObject,
  type NonIndexRouteObject,
  type RouteObject,
} from 'react-router-dom';
import type { ReadonlyTuple } from 'type-fest';
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
  return module ? 'default' in module && typeof module.default === 'function' : true;
}

export type RouteGroup = readonly RouteObject[];
export type WritableRouteGroup = RouteObject[];

export type RouteTransformer<T> = (
  original: RouteObject | undefined,
  overriding: T | undefined,
  children?: RouteGroup,
) => RouteObject | undefined;

export type RouteGroupSplitter = Readonly<{
  numberOfGroups: number;
  finalize?(groups: ReadonlyArray<RouteGroup | undefined>): RouteGroup;
  split(
    groups: ReadonlyArray<RouteGroup | undefined>,
    route: RouteObject,
    next: (...nodes: RouteGroup) => ReadonlyArray<RouteGroup | undefined>,
  ): ReadonlyArray<RouteGroup | undefined>;
}>;

export type RouteSplittingRule = (route: RouteObject) => boolean;

type RoutesModifier = (routes: RouteGroup | undefined) => RouteGroup | undefined;

function createRouteEntry<T extends RouteBase>(route: T): readonly [key: string, value: T] {
  return [`${route.path ?? ''}-${route.children ? 'n' : 'i'}`, route];
}

enum RouteHandleFlags {
  FLOW_LAYOUT = 'flowLayout',
  IGNORE_FALLBACK = 'ignoreFallback',
  SKIP_LAYOUT = 'skipLayout',
}

function hasRouteHandleFlag<T extends RouteHandleFlags>(route: RouteObject, flag: T): boolean {
  return typeof route.handle === 'object' && flag in route.handle && (route.handle as Record<T, boolean>)[flag];
}

function split<T extends readonly RouteSplittingRule[]>(
  originalRoutes: RouteGroup,
  ...rules: T
): readonly [...ReadonlyTuple<RouteGroup, T['length']>, RouteGroup] {
  // Split the routes tree onto two subtrees with and without
  // a server layout.
  return transformTree<RouteGroup, readonly RouteGroup[]>(originalRoutes, (routes, next) =>
    // Split single routes list onto two filtered lists
    routes.reduce<readonly WritableRouteGroup[]>(
      (groups, route) => {
        for (let i = 0; i < groups.length; i++) {
          if (rules[i]?.(route)) {
            groups[i].push(route);
            return groups;
          }
        }

        if (!route.children?.length) {
          groups.at(-1)?.push(route);
          return groups;
        }

        const childrenGroups = next(...route.children);

        for (let i = 0; i < groups.length; i++) {
          if (childrenGroups[i].length) {
            groups[i].push({
              ...route,
              children: childrenGroups[i],
            } as RouteObject);
          }
        }

        return groups;
      },
      new Array(rules.length + 1).fill([]),
    ),
  ) as readonly [...ReadonlyTuple<RouteGroup, T['length']>, RouteGroup];
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
  withReactRoutes(routes: RouteGroup): this {
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
          throw new Error(`The module for the "${path}" section doesn't have the React component exported by default`);
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
    const fallbackRoutes: RouteGroup = [
      { path: '*', element: createElement(component), handle: config },
      { index: true, element: createElement(component), handle: config },
    ];

    this.update(fallbackRoutes, (original, added, children) => {
      if (original && !hasRouteHandleFlag(original, RouteHandleFlags.IGNORE_FALLBACK)) {
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

      const [serverGroup, clientGroup] = split(originalRoutes, (route) =>
        hasRouteHandleFlag(route, RouteHandleFlags.FLOW_LAYOUT),
      );

      return [
        ...(serverGroup.length
          ? [
              {
                element: createElement(layoutComponent),
                children: serverGroup as RouteObject[],
                handle: {
                  [RouteHandleFlags.IGNORE_FALLBACK]: true,
                },
              },
            ]
          : []),
        ...clientGroup,
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
      transformTree<[RouteGroup | undefined, readonly T[] | undefined], RouteGroup | undefined>(
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
    const routes = this.#modifiers.reduce<RouteGroup | undefined>((acc, mod) => mod(acc) ?? acc, undefined) ?? [];

    return {
      routes,
      router: createBrowserRouter([...routes], { basename: new URL(document.baseURI).pathname, ...options }),
    };
  }

  #withLayoutSkipping(): this {
    this.#modifiers.push((originalRoutes) => {
      if (!originalRoutes) {
        return originalRoutes;
      }

      const [noLayoutGroup, layoutGroup] = split(originalRoutes, (route) =>
        hasRouteHandleFlag(route, RouteHandleFlags.SKIP_LAYOUT),
      );

      const finalNoLayoutGroup = transformTree<RouteGroup, RouteGroup>(noLayoutGroup, (routes, next) =>
        routes.map((route) => {
          if (hasRouteHandleFlag(route, RouteHandleFlags.SKIP_LAYOUT)) {
            return route;
          }

          const { element, ...rest } = route;
          return route.children?.length
            ? ({
                ...rest,
                children: next(...route.children),
              } as RouteObject)
            : rest;
        }),
      );

      return [
        ...(finalNoLayoutGroup.length
          ? [
              {
                children: finalNoLayoutGroup as RouteObject[],
                handle: {
                  [RouteHandleFlags.IGNORE_FALLBACK]: true,
                },
              },
            ]
          : []),
        ...layoutGroup,
      ];
    });

    return this;
  }
}
