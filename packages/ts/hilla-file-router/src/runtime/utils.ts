import type { ComponentType } from 'react';
import type { RouteParamType } from '../vite-plugin/utils.js';

export type ViewConfig = Readonly<{
  /**
   * View title used in the main layout header, as <title> and as the default
   * for the menu entry. If not defined, the component name will be taken,
   * transformed from camelCase.
   */
  title?: string;

  /**
   * A map of route parameters and their types.
   */
  params?: Readonly<Record<string, RouteParamType>>;

  /**
   * Same as in the explicit React Router configuration.
   */
  rolesAllowed?: string[];

  /**
   * Allows overriding the route path configuration. Uses the same syntax as
   * the path property with React Router. This can be used to define a route
   * that conflicts with the file name conventions, e.g. /foo/index.
   */
  route?: string;

  /**
   * Controls whether the view implementation will be lazy loaded the first time
   * it's used or always included in the bundle. If set to undefined (which is
   * the default), views mapped to / and /login will be eager and any other view
   * will be lazy (this is in sync with defaults in Flow)
   */
  lazy?: boolean;

  /**
   * If set to false, then the route will not be registered with React Router,
   * but it will still be included in the main menu and used to configure
   * Spring Security
   */
  register?: boolean;

  menu?: Readonly<{
    /**
     * Title to use in the menu. Falls back the title property of the view
     * itself if not defined.
     */
    title?: string;

    /**
     * Used to determine the order in the menu. Ties are resolved based on the
     * used title. Entries without explicitly defined ordering are put below
     * entries with an order.
     */
    order?: number;
    /**
     * Set to true to explicitly exclude a view from the automatically
     * populated menu.
     */
    exclude?: boolean;
    /**
     * Icon to use in the menu.
     */
    icon?: string;
  }>;
}>;

/**
 * A module that exports a React component and an optional view configuration.
 */
export type RouteModule<P = object> = Readonly<{
  default: ComponentType<P>;
  config?: ViewConfig;
}>;

/**
 * A framework-agnostic object generated from the file-based route.
 */
export type AgnosticRoute<T> = Readonly<{
  path: string;
  module?: T;
  children?: ReadonlyArray<AgnosticRoute<T>>;
}>;

/**
 * Transforms the whole route tree into a new format.
 *
 * @param route - The route to transform.
 * @param getChildren - A function that returns the children of the route.
 * @param transformer - A function that transforms the route and its children.
 *
 * @returns The transformed route.
 */
export function transformRoute<T, U>(
  route: T,
  getChildren: (route: T) => IterableIterator<T> | null | undefined,
  transformer: (route: T, children: readonly U[]) => U,
): U {
  const children = getChildren(route);

  return transformer(
    route,
    children ? Array.from(children, (child) => transformRoute(child, getChildren, transformer)) : [],
  );
}

const viewPattern = /view/giu;
const upperCaseSplitPattern = /(?=[A-Z])/gu;

/**
 * Extracts the name of a component.
 *
 * @param component - The component to extract the name from.
 *
 * @returns The name of the component.
 */
export function convertComponentNameToTitle(component?: unknown): string | undefined {
  if (
    component &&
    (typeof component === 'object' || typeof component === 'function') &&
    'name' in component &&
    typeof component.name === 'string'
  ) {
    return component.name.replace(viewPattern, '').split(upperCaseSplitPattern).join(' ');
  }

  return undefined;
}

/**
 * Processes the route configuration to ensure that the title is set.
 *
 * @param config - The route configuration to process.
 * @param componentName - The name of the component. Could be extracted from the component using
 * {@link convertComponentNameToTitle}.
 *
 * @returns The processed route configuration.
 */
export function adjustRouteConfig(config?: ViewConfig, componentName?: string): ViewConfig | undefined {
  if (config?.title) {
    return config;
  }

  if (componentName) {
    return {
      ...config,
      title: componentName.replace(viewPattern, '').split(upperCaseSplitPattern).join(' '),
    };
  }

  return config;
}
