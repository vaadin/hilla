/* eslint-disable @typescript-eslint/no-use-before-define */
import type { ComponentType } from 'react';
import { createBrowserRouter, type RouteObject } from 'react-router';
import type { AgnosticRoute, RouterBuildOptions, RouterConfiguration, ViewConfig } from '../../types.js';
import createFallbackTransformer, { createFallbackRoutes } from './createFallbackTransformer.js';
import createProtectTransformer from './createProtectTransformer.js';
import fileRouteTransformer from './fileRouteTransformer.js';
import mergeLayout from './mergeLayout.js';
import { mergeRouteTrees } from './mergeRouteTrees.js';
import mergeSkipLayouts from './mergeSkipLayout.js';
import type { RouteLike, RouteTreeModifier, RouteTransformer } from './utils.js';

/**
 * A configuration builder for creating a Vaadin-specific router for React with
 * authentication and server routes support.
 *
 * The configuration builder allows you to compose and modify route trees by
 * chaining methods that add custom React routes, generated file-based routes,
 * layout components, etc. Modifiers are accumulated and applied in order when
 * building the final router configuration.
 *
 * @example
 * ```typescript
 * const { routes, router } = new RouteConfigurationBuilder()
 *   .withFileRoutes(fileRoutes)
 *   .withReactRoutes({ path: '/foo/baz', element: <FooBazPage /> })
 *   .withFallback(Flow)
 *   .protect('/login')
 *   .build();
 * ```
 *
 * @public
 */
export class RouterConfigurationBuilder {
  readonly #modifiers: RouteTreeModifier[] = [];
  #isFallbackSet = false;
  #isLayoutSet = false;

  /**
   * Adds the given React routes to the current list of routes. All the routes
   * are deeply merged to preserve the path uniqueness.
   *
   * @param routes - An array of React Router route objects to be merged into
   * the current route list.
   *
   * @returns The current instance of the builder for method chaining.
   */
  withReactRoutes(routes: readonly RouteObject[]): this {
    this.update(routes);
    return this;
  }

  /**
   * Adds the given file routes to the current list of routes. All the routes
   * are transformed to React RouterObjects and deeply merged to preserve the
   * path uniqueness.
   *
   * @param routes - An array of file-based route objects to be processed and
   * merged into the current route list.
   *
   * @returns The current instance of the builder for method chaining.
   */
  withFileRoutes(routes: readonly AgnosticRoute[]): this {
    this.update(routes, fileRouteTransformer);
    return this;
  }

  /**
   * Adds a fallback component for each branch of the current route tree.
   *
   * The fallback component is used when no other route matches the requested
   * URL. In terms of Vaadin application, after no match on the client side, the
   * turn goes to the server-side router.
   *
   * @remarks This method can be called only once. All the subsequent calls will
   * be ignored.
   *
   * @remarks This method also runs the `withLayout` method with the given
   * component to make sure server-side layout is applied to routes.
   *
   * @param component - The component to use as the fallback and layout
   * component.
   * @param config - Optional view configuration for the fallback component.
   *
   * @returns The current instance of the builder for method chaining.
   */
  withFallback(component: ComponentType, config?: ViewConfig): this {
    if (!this.#isFallbackSet) {
      this.#isFallbackSet = true;
      this.withLayout(component);

      const fallbackRoutes = createFallbackRoutes(component, config);

      this.update(
        // Add the fallback routes to the end of the route tree.
        fallbackRoutes,
        // Add the fallback routes to each route tree branch via transformer.
        createFallbackTransformer(fallbackRoutes),
      );
    }
    return this;
  }

  /**
   * Adds the parent layout to all views with the `flowLayouts` flag set in the
   * ViewConfiguration.
   *
   * @remarks This method can be called only once. All the subsequent calls will
   * be ignored.
   *
   * @param component - The component to use as the layout for the routes.
   * Usually, it is `Flow` component.
   *
   * @returns The current instance of the builder for method chaining.
   */
  withLayout(component: ComponentType): this {
    if (!this.#isLayoutSet) {
      this.#isLayoutSet = true;
      this.#modifiers.push((originalRoutes) => mergeLayout(originalRoutes, component));
    }
    return this;
  }

  /**
   * Adds protection to the route, requiring authentication or authorization to
   * access it.
   *
   * @param redirectPath - Optional path to redirect to when protection fails.
   * If not provided, the default redirect page (`/login`) will be used.
   *
   * @returns The current instance of the builder for method chaining.
   */
  protect(redirectPath?: string): this {
    this.update(undefined, createProtectTransformer(redirectPath));
    return this;
  }

  /**
   * Deeply updates the current route tree, merging the existing routes with the
   * given routes using the provided transformer callback.
   *
   * @param routes - The routes used to update the current route tree.
   * @param callback - A transformer function that defines how the routes should
   * be modified. Required if `routes` are not provided.
   *
   * @returns This RouteConfigurationBuilder instance for method chaining
   */
  update(routes: undefined, callback: RouteTransformer): this;

  /**
   * Deeply updates the current route tree, merging the existing routes with the
   * given routes using the provided transformer callback.
   *
   * @typeParam T - The type of routes being updated.
   *
   * @param routes - The routes used to update the current route tree.
   * @param callback - A transformer function that defines how the routes should
   * be modified. Required if `routes` are not provided.
   *
   * @returns This RouteConfigurationBuilder instance for method chaining
   */
  update<T extends RouteLike>(routes: readonly T[], callback?: RouteTransformer<T>): this;
  update<T extends RouteLike>(routes: readonly T[] | undefined, callback: RouteTransformer<T>): this {
    this.#modifiers.push((originalRoutes) => mergeRouteTrees(originalRoutes, routes, callback));
    return this;
  }

  /**
   * Builds the router configuration by applying all registered modifiers to the
   * routes.
   *
   * @remarks
   * This method applies the the logic for layout skipping along with any other
   * registered modifiers to transform the routes.
   *
   * @param options - Optional React `createBrowserRouter` options to configure
   * the router.
   *
   * @returns A RouterConfiguration object containing the processed routes and
   * configured browser router
   */
  build(options?: RouterBuildOptions): RouterConfiguration {
    this.#modifiers.push((originalRoutes) => mergeSkipLayouts(originalRoutes));
    const routes =
      this.#modifiers.reduce<readonly RouteObject[] | undefined>((acc, mod) => mod(acc) ?? acc, undefined) ?? [];

    return {
      routes,
      router: createBrowserRouter([...routes], { basename: new URL(document.baseURI).pathname, ...options }),
    };
  }
}
