export type ViewConfig = Readonly<{
  /**
   * View title used in the main layout header, as <title> and as the default
   * for the menu entry. If not defined, then the view function name is converted
   * from CamelCase after removing any "View" postfix.
   */
  title?: string;

  /**
   * Same as in the explicit React Router configuration.
   */
  rolesAllowed?: string[];

  /**
   * Allows overriding the route path configuration. Uses the same syntax as
   * the path property with React Router. This can be used to define a route
   * that conflicts with the file name conventions, e.g. /foo/index
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
  }>;
}>;

export type AgnosticRoute<T> = Readonly<{
  path: string;
  module?: T;
  children?: ReadonlyArray<AgnosticRoute<T>>;
}>;

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

export function extractComponentName(component?: unknown): string | undefined {
  if (
    component &&
    (typeof component === 'object' || typeof component === 'function') &&
    'name' in component &&
    typeof component.name === 'string'
  ) {
    return component.name;
  }

  return undefined;
}

const viewPattern = /view/giu;
const upperCaseSplitPattern = /(?=[A-Z])/gu;

export function adjustViewTitle(config?: ViewConfig, componentName?: string): ViewConfig | undefined {
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
