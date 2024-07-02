import type { UIMatch } from '@remix-run/router';
import { useLoaderData, useMatches } from 'react-router-dom';
import type { ViewConfig } from '../types.js';

/**
 * Hook to return the {@link ViewConfig} for the current route.
 */
export function useViewConfig<M extends ViewConfig>(): M | undefined {
  const matches = useMatches() as ReadonlyArray<UIMatch<unknown, M>>;
  return matches[matches.length - 1]?.handle;
}

/**
 * Hook to return the data for the current route. This data is loaded by the
 * loader function of the {@link ViewConfig} for the current route.
 *
 * @param loader - Loader function or {@link ViewConfig}. This is not actually
 * used here, but it is needed to infer the return type.
 * @returns The data loaded by the loader function of the current route.
 */
export function useLoader<T, P extends unknown[]>(loader: ViewConfig<T> | ((...params: P) => Promise<T>)): T {
  return useLoaderData() as T;
}
