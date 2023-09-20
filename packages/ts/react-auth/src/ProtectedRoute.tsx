import { useContext } from 'react';
import type { RouteObject } from 'react-router-dom';
import { type IndexRouteObject, Navigate, type NonIndexRouteObject, useLocation } from 'react-router-dom';
import { type AccessProps, AuthContext } from './useAuth.js';

type CustomMetadata = Record<string, any>;

type HandleWithAuth = Readonly<{ handle?: AccessProps & CustomMetadata }>;

type Override<T, E> = E & Omit<T, keyof E>;

type IndexRouteObjectWithAuth = Override<IndexRouteObject, HandleWithAuth>;
type NonIndexRouteObjectWithAuth = Override<
  Override<NonIndexRouteObject, HandleWithAuth>,
  {
    children?: RouteObjectWithAuth[];
  }
>;
export type RouteObjectWithAuth = IndexRouteObjectWithAuth | NonIndexRouteObjectWithAuth;

interface ProtectedRouteProps {
  redirectPath: string;
  access: AccessProps;
  element: JSX.Element;
}

function ProtectedRoute({ redirectPath, access, element }: ProtectedRouteProps): JSX.Element | null {
  const {
    state: { initializing, loading, user },
    hasAccess,
  } = useContext(AuthContext);

  const location = useLocation();

  if (initializing || loading) {
    return <div></div>;
  }

  if (!hasAccess(access)) {
    return <Navigate to={redirectPath} state={{ from: location }} replace />;
  }

  return element;
}

const collectRoutes = <T,>(routes: T[]): T[] => {
  const allRoutes: T[] = [];
  routes.forEach((route) => {
    allRoutes.push(route);
    if ((route as RouteObject).children !== undefined) {
      allRoutes.push(...collectRoutes((route as RouteObject).children as T[]));
    }
  });
  return allRoutes;
};

/**
 * Adds protection to routes that require authentication.
 * These routes should contain the {@link AccessProps.requiresLogin} and/or
 * {@link AccessProps.rolesAllowed} properties.
 *
 * @param routes - the routes to check if any of them needs to be protected
 * @param redirectPath - the path to redirect to if the route is
 * protected and the user is not authenticated.
 * @returns the routes extended with protection if needed
 */
export const protectRoutes = (
  routes: RouteObjectWithAuth[],
  redirectPath: string = '/login',
): RouteObjectWithAuth[] => {
  const allRoutes: RouteObjectWithAuth[] = collectRoutes(routes);

  allRoutes.forEach((route) => {
    const { handle } = route;
    const requiresAuth = handle?.requiresLogin ?? handle?.rolesAllowed?.length;

    if (requiresAuth) {
      route.element = (
        <ProtectedRoute
          redirectPath={redirectPath}
          access={route.handle as AccessProps}
          element={route.element as JSX.Element}
        />
      );
    }
  });

  return routes;
};
