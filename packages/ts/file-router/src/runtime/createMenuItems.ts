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
 * Creates menu items from the views provided by the server. The views are sorted according to the
 * {@link ViewConfig.menu.order}, filtered out if they are explicitly excluded via {@link ViewConfig.menu.exclude}.
 * Note that views with no order are put below views with an order. Ties are resolved based on the path string
 * comparison.
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
        to: path,
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
