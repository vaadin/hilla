import { protectRoute } from '@vaadin/hilla-react-auth';
import type { RouteObject } from 'react-router';
import type { RouteTransformer } from './utils.js';

/**
 * Creates a route transformer that applies route protection to a given route,
 * optionally redirecting unauthorized access to a specified path.
 *
 * @param redirectPath - Optional path to redirect unauthorized users to. If not
 * provided, redirects to '/login'.
 *
 * @returns A route transformer function that applies protection to the route.
 */
export default function createProtectTransformer(redirectPath?: string): RouteTransformer {
  return ({ original, children }) => {
    if (!original) {
      return original;
    }

    const finalRoute = protectRoute(original, redirectPath);
    finalRoute.children = children as RouteObject[] | undefined;
    return finalRoute;
  };
}
