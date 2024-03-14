export type ViewConfig = Readonly<{
  /**
   * View title used in the main layout header, as <title> and as the default
   * for the menu entry. If not defined, the component name will be taken,
   * transformed from camelCase.
   */
  title?: string;

  /**
   * Same as in the explicit React Router configuration.
   */
  rolesAllowed?: readonly string[];

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
  children?: ReadonlyArray<AgnosticRoute<T>>;
}>;

/**
 * A menu item used in for building the navigation menu.
 */
export type MenuItem = Readonly<{
  to: string;
  icon?: string;
  title?: string;
}>;
