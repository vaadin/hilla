import { type IndexRouteObject, type NonIndexRouteObject, useMatches } from 'react-router-dom';

export type MenuProps = Readonly<{
  icon?: string;
  title?: string;
}>;

export type AccessProps = Readonly<{
  requireAuthentication?: boolean;
  rolesAllowed?: readonly string[];
}>;

export type ViewMeta = Readonly<{ handle?: AccessProps & MenuProps }>;

type Override<T, E> = E & Omit<T, keyof E>;

export type IndexHillaRouteObject = Override<IndexRouteObject, ViewMeta>;
export type NonIndexHillaRouteObject = Override<
  Override<NonIndexRouteObject, ViewMeta>,
  {
    children?: HillaRouteObject[];
  }
>;
export type HillaRouteObject = IndexHillaRouteObject | NonIndexHillaRouteObject;

type RouteMatch = ReturnType<typeof useMatches> extends Array<infer T> ? T : never;

export type HillaRouteMatch = Readonly<Override<RouteMatch, ViewMeta>>;

export const useHillaMatches = useMatches as () => readonly HillaRouteMatch[];
