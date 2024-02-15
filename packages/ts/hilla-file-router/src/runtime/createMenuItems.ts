import type { ViewConfig } from '../types.js';

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
 * Creates menu items from the views provided by the server.
 *
 * @param vaadinObject - The Vaadin object containing the server views.
 * @returns A list of menu items.
 */
export function createMenuItems(vaadinObject = window.Vaadin): readonly MenuItem[] {
  return vaadinObject?.server?.views
    ? Object.entries(vaadinObject.server.views)
        // Sort views according to the order specified in the view configuration.
        .sort(([_a, a], [_b, b]) => (a.menu?.order ?? 0) - (b.menu?.order ?? 0))
        // Filter out the views that are explicitly excluded from the menu.
        .filter(([_key, value]) => (value.menu ? !value.menu.exclude : true))
        // Map the views to menu items.
        .map(([path, config]) => ({
          to: path,
          icon: config.menu?.icon,
          title: config.menu?.title ?? config.title,
        }))
    : [];
}
