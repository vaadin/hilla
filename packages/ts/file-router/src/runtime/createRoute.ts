import type { ComponentType } from 'react';
import type { AgnosticRoute, Module, RouteModule, ViewConfig } from '../types.js';

/**
 * Extends a router module's config with additional properties. The original
 * module config is preferred over the extension.
 *
 * @param module - The module to extend.
 * @param config - The extension config.
 * @returns
 *
 * @deprecated Use object spread syntax instead.
 */
export function extendModule(module: Module | null, config?: ViewConfig): Module {
  return {
    ...module,
    config: {
      ...config,
      ...(module?.config as ViewConfig),
    },
  };
}

/**
 * Create a single framework-agnostic route object. Later, it can be transformed into a framework-specific route object,
 * e.g., the one used by React Router.
 *
 * @param path - A route path segment.
 * @param children - An array of child routes.
 *
 * @returns A framework-agnostic route object.
 */
export function createRoute(path: string, children?: readonly AgnosticRoute[]): AgnosticRoute;
/**
 * Create a single framework-agnostic route object. Later, it can be transformed into a framework-specific route object,
 * e.g., the one used by React Router.
 *
 * @param path - A route path segment.
 * @param module - A module that exports a component and an optional config object.
 * @param children - An array of child routes.
 *
 * @deprecated Use `createRoute(path, component, config, children)` instead.
 */
export function createRoute(path: string, module: Module, children?: readonly AgnosticRoute[]): AgnosticRoute;
/**
 * Create a single framework-agnostic route object. Later, it can be transformed into a framework-specific route object,
 * e.g., the one used by React Router.
 *
 * @param path - A route path segment.
 * @param component - A React component.
 * @param config - An optional config object.
 * @param children - An array of child routes.
 */
export function createRoute(
  path: string,
  component: ComponentType,
  config: ViewConfig,
  children?: readonly AgnosticRoute[],
): AgnosticRoute;
export function createRoute(
  path: string,
  moduleOrChildrenOrComponent?: Module | ComponentType | readonly AgnosticRoute[],
  childrenOrConfig?: readonly AgnosticRoute[] | ViewConfig,
  children?: readonly AgnosticRoute[],
): AgnosticRoute {
  let component: ComponentType | undefined;
  let config: ViewConfig | undefined;
  if (Array.isArray(moduleOrChildrenOrComponent)) {
    // eslint-disable-next-line no-param-reassign
    children = moduleOrChildrenOrComponent;
  } else if (typeof moduleOrChildrenOrComponent === 'function') {
    component = moduleOrChildrenOrComponent;
    config = childrenOrConfig as ViewConfig;
  } else if (moduleOrChildrenOrComponent) {
    ({ default: component, config } = moduleOrChildrenOrComponent as RouteModule);
  }

  return {
    path,
    module: { default: component, config },
    component,
    config,
    children,
  };
}
