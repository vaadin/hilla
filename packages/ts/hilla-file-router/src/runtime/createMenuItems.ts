import traverse from '../shared/traverse.js';
import {
  adjustRouteConfig,
  type AgnosticRoute,
  extractComponentName,
  type RouteModule,
  type ViewConfig,
} from './utils.js';

export type VaadinServer = Readonly<{
  views: Record<string, ViewConfig>;
}>;

export type VaadinObject = Readonly<{
  server?: VaadinServer;
}>;

declare global {
  interface Window {
    Vaadin?: VaadinObject;
  }
}

export type MenuItem = Readonly<{
  to: string;
  icon?: string;
  title?: string;
}>;

/**
 * Create a list of menu items from the generated file-based routes and the server views.
 *
 * @param routes - The generated file-based routes.
 * @param vaadinObject - The Vaadin object containing the server views.
 * @returns The list of menu items.
 */
export function createMenuItems(routes: AgnosticRoute<RouteModule>, vaadinObject = window.Vaadin): readonly MenuItem[] {
  const serverViews = vaadinObject?.server?.views ?? {};

  const frontendViews = Array.from(traverse(routes), (branch) => {
    const leaf = branch[branch.length - 1].module;
    const value = adjustRouteConfig(leaf?.config, extractComponentName(leaf?.default));
    const key = branch.map(({ path, module }) => module?.config?.route ?? path).join('/');

    return [key, value] as const;
  });

  return (
    [...frontendViews, ...Object.entries(serverViews)]
      .sort(([_a, a], [_b, b]) => (a?.menu?.order ?? 0) - (b?.menu?.order ?? 0))
      // Filter out the views that are explicitly excluded from the menu.
      .filter(([_key, value]) => (value?.menu ? !value.menu.exclude : true))
      .map(([path, config]) => ({
        to: path,
        icon: config?.menu?.icon,
        title: config?.menu?.title ?? config?.title,
      }))
  );
}
