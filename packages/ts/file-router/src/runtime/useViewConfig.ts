import type { UIMatch } from 'react-router';
import { useMatches } from 'react-router';
import type { ViewConfig } from '../types.js';

/**
 * Hook to return the {@link ViewConfig} for the current route.
 */
export function useViewConfig<M extends ViewConfig>(): M | undefined {
  const matches = useMatches() as ReadonlyArray<UIMatch<unknown, M>>;
  return matches[matches.length - 1]?.handle;
}
