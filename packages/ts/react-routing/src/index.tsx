import { type IndexRouteObject, type NonIndexRouteObject, useMatches } from 'react-router-dom';

export type MenuProps = Readonly<{
  icon?: string;
  title?: string;
}>;

export type AccessProps = Readonly<{
  requireAuthentication?: boolean;
  rolesAllowed?: readonly string[];
}>;

export type RouteMetadata = AccessProps & MenuProps;

type HandleWithMetadata = { handle?: RouteMetadata };

type Override<T, E> = E & Omit<T, keyof E>;

export type IndexRouteObjectWithMetadata = Override<IndexRouteObject, HandleWithMetadata>;
export type NonIndexRouteObjectWithMetadata = Override<
  Override<NonIndexRouteObject, HandleWithMetadata>,
  {
    children?: RouteObjectWithMetadata[];
  }
>;
export type RouteObjectWithMetadata = IndexRouteObjectWithMetadata | NonIndexRouteObjectWithMetadata;

export function useRouteMetadata(): RouteMetadata | undefined {
  const matches = useMatches();
  const match = [...matches].reverse().find((m) => m.handle);
  return match?.handle as RouteMetadata | undefined;
}
