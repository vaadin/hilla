import type { createBrowserRouter, RouteObject } from 'react-router-dom';

export type ViewConfig<T = unknown> = Readonly<{
  /**
   * Loader function that is called when the view is accessed. The loader
   * function is expected to return a Promise that resolves to the component
   * to render. The loader function is called with the parameters passed to
   * the view, in the order they are present in the URL. The params will hence
   * be strings and will be deserialized on the server side, so the function
   * should accept simple types like strings and numbers.
   */
  loader?(...params: unknown[]): Promise<T>;

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
 * An unknown JavaScript module.
 */
export type Module = Readonly<Record<string, unknown>>;

/**
 * A module that exports a component and an optional view configuration.
 */
export type RouteModule<C = unknown> = Module &
  Readonly<{
    default: C;
    config?: ViewConfig;
  }>;

/**
 * A framework-agnostic object generated from the file-based route.
 */
export type AgnosticRoute = Readonly<{
  path: string;
  module?: Module;
  children?: readonly AgnosticRoute[];
}>;

/**
 * A menu item used in for building the navigation menu.
 */
export type MenuItem = Readonly<{
  to: string;
  icon?: string;
  title?: string;
}>;

export type RouterConfiguration = Readonly<{
  routes: readonly RouteObject[];
  router: ReturnType<typeof createBrowserRouter>;
}>;

export type RouterBuildOptions = Parameters<typeof createBrowserRouter>[1];
