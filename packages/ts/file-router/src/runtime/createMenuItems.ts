/// <reference types="vite/client" />
import { type Signal, signal } from '@vaadin/hilla-react-signals';
import type { VaadinWindow } from '../shared/internal.js';
import type { MenuItem, ViewConfig } from '../types.js';

export const viewsSignal: Signal<Readonly<Record<string, Readonly<ViewConfig>>> | undefined> = signal(
  (window as VaadinWindow).Vaadin?.views,
);

function isExcluded(value: ViewConfig): boolean {
  return !!value.menu?.exclude;
}

function hasVariablePathSegment(path: string): boolean {
  return path.split('/').some((segment) => segment.startsWith(':'));
}

/**
 * Converts a route path to a path relative to the application base URI by removing the leading slash. This way the
 * path keeps pointing to the correct view even when the application is deployed under a context path, e.g. when it is
 * used as the `path` of a side nav item or as the `href` of a link.
 */
function toBaseRelativePath(path: string): string {
  return path.startsWith('/') ? path.substring(1) : path;
}

/**
 * Creates menu items from the views provided by the server. The views are sorted according to the
 * {@link ViewConfig.menu.order}, filtered out if they are explicitly excluded via {@link ViewConfig.menu.exclude}.
 * Note that views with no order are put below views with an order. Ties are resolved based on the path string
 * comparison.
 *
 * The `to` property of the returned items is relative to the application base URI, i.e. it has no leading slash, so
 * that it also resolves correctly when the application is deployed under a context path. It can be used as is as the
 * `path` of a side nav item or the `href` of a link. When it is given to a React Router API instead, such as
 * `navigate()` or `<Link>`, from a layout that is not the root one, prefix it with a slash, e.g.
 * `navigate('/' + to)`, so that it is resolved from the router root instead of the enclosing route.
 *
 * @returns A list of menu items.
 */
export function createMenuItems<T = unknown>(): ReadonlyArray<MenuItem<T>> {
  // @ts-expect-error: esbuild injection
  // eslint-disable-next-line @typescript-eslint/no-unsafe-call
  __REGISTER__('createMenuItems', (window as VaadinWindow).Vaadin);
  const collator = new Intl.Collator('en-US');
  if (!viewsSignal.value) {
    return [];
  }

  const views = Object.entries(viewsSignal.value);

  return (
    views
      // Filter out the views that are explicitly excluded from the menu.
      .filter(([path, value]) => !isExcluded(value) && !hasVariablePathSegment(path))
      // Map the views to menu items.
      .map(([path, config]) => ({
        to: toBaseRelativePath(path),
        icon: config.menu?.icon,
        title: config.menu?.title ?? config.title,
        order: config.menu?.order,
        detail: config.detail as T | undefined,
      }))
      // Sort views according to the order specified in the view configuration.
      .sort((menuA, menuB) => {
        const ordersDiff = (menuA.order ?? Number.MAX_VALUE) - (menuB.order ?? Number.MAX_VALUE);
        return ordersDiff !== 0 ? ordersDiff : collator.compare(menuA.to, menuB.to);
      })
  );
}

if (import.meta.hot) {
  import.meta.hot.on('fs-route-update', () => {
    fetch('?v-r=routeinfo')
      .then(async (resp) => resp.json())
      .then((json) => {
        viewsSignal.value = json;
      })
      .catch((e: unknown) => {
        console.error('Failed to fetch route info', e);
      });
  });
}
