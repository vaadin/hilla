/* -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { useContext, type ReactNode } from 'react';
import type { RouteObject } from 'react-router-dom';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { type AccessProps, AuthContext } from './useAuth.js';

interface ProtectedRouteProps {
  redirectPath: string;
  route: ReactNode;
}

function ProtectedRoute({ redirectPath, route }: ProtectedRouteProps): JSX.Element | null {
  const {
    state: { initializing, user },
  } = useContext(AuthContext);

  const location = useLocation();

  if (initializing) {
    return <div></div>;
  }

  if (!user) {
    return <Navigate to={redirectPath} state={{ from: location }} replace />;
  }

  return route ? (route as JSX.Element) : <Outlet />;
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

export type RouteObjectWithAuth = RouteObject & {
  handle?: AccessProps;
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
    const { handle } = route as AccessProps;

    if (handle?.requiresLogin ?? handle?.rolesAllowed) {
      route.element = <ProtectedRoute redirectPath={redirectPath} route={(route as RouteObject).element} />;
    }
  });

  return routes;
};
