import type { ComponentType } from 'react';
import type { createBrowserRouter, RouteObject } from 'react-router';

/**
 * A configuration object for a view. This is used to define the view's
 * metadata, such as the title, roles allowed, and other properties.
 *
 * @typeParam T - The type of the detail object.
 */
export type ViewConfig<T = unknown> = Readonly<{
  /**
   * View title used in the main layout header, as <title> and as the default
   * for the menu entry. If not defined, the component name will be taken,
   * transformed from camelCase.
   */
  title?: string;

  /**
   * Same as in the explicit React Router configuration.
   */
  rolesAllowed?: readonly [string, ...string[]];

  /**
   * Set to true to require the user to be logged in to access the view.
   */
  loginRequired?: boolean;

  /**
   * Allows overriding the route path configuration. Uses the same syntax as
   * the path property with React Router. This can be used to define a route
   * that conflicts with the file name conventions, e.g. /foo/index.
   */
  route?: string;

  /**
   * Set to true to indicate that the view is using server side parent layout
   * annotated with the Layout annotation.
   */
  flowLayout?: boolean;

  /**
   * Set to true to make the view render without enclosing in any layouts.
   */
  skipLayouts?: boolean;

  /**
   * Set false to indicate that the view should not be lazy loaded. `/` and
   * `/login` are always loaded eagerly.
   *
   * @defaultValue `true`
   */
  lazy?: boolean;

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

  /**
   * Used to add additional properties to the view. This object will be
   * available when building the menu.
   *
   * @see {@link ./runtime/createMenuItems.ts#createMenuItems}
   */
  detail?: T;
}>;

/**
 * An unknown JavaScript module.
 */
export type Module = Readonly<Record<string, unknown>>;

/**
 * A module that exports a component and an optional view configuration.
 */
export type RouteModule<C extends ComponentType = ComponentType> = Module &
  Readonly<{
    default?: C;
    config?: ViewConfig;
  }>;

/**
 * A framework-agnostic object generated from the file-based route.
 */
export type AgnosticRoute = Readonly<{
  path: string;
  /**
   * @deprecated Use `component` and `config` separately instead.
   */
  module?: RouteModule;
  component?: ComponentType;
  config?: ViewConfig;
  children?: readonly AgnosticRoute[];
  flowLayout?: boolean;
}>;

/**
 * A menu item used in for building the navigation menu.
 *
 * @typeParam T - The type of the detail object, same as in the view configuration.
 */
export type MenuItem<T = unknown> = Readonly<{
  to: string;
  icon?: string;
  title?: string;
  detail?: T;
}>;

export type RouterConfiguration = Readonly<{
  routes: readonly RouteObject[];
  router: ReturnType<typeof createBrowserRouter>;
}>;

export type RouterBuildOptions = Parameters<typeof createBrowserRouter>[1];
