import { expect } from '@esm-bundle/chai';
import { render, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { configureAuth, protectRoutes, type RouteObjectWithAuth } from '../src';

function TestView({ route }: { route: string }) {
  return <div>{`route: ${route}`}</div>;
}

const testRoutes: RouteObjectWithAuth[] = [
  {
    path: '/login',
    element: <TestView route="/login" />,
  },
  {
    path: '/public',
    element: <TestView route="/public" />,
  },
  {
    path: '/protected/login',
    element: <TestView route="/protected/login" />,
    handle: {
      loginRequired: true,
    },
  },
  {
    path: '/protected/role/user',
    element: <TestView route="/protected/role/user" />,
    handle: {
      loginRequired: true,
      rolesAllowed: ['user'],
    },
  },
  {
    path: '/protected/role/admin',
    element: <TestView route="/protected/role/admin" />,
    handle: {
      requiresLogin: true, // deprecated
      rolesAllowed: ['admin'],
    },
  },
  {
    path: '/protected/roleonly/user',
    element: <TestView route="/protected/roleonly/user" />,
    handle: {
      rolesAllowed: ['user'],
    },
  },
  {
    path: '/protected/roleonly/admin',
    element: <TestView route="/protected/roleonly/admin" />,
    handle: {
      rolesAllowed: ['admin'],
    },
  },
];

interface User {
  roles: string[];
}

function TestApp({ user, initialRoute }: { user?: User; initialRoute: string }) {
  const { AuthProvider, useAuth } = configureAuth(async () => Promise.resolve(user));
  const protectedRoutes = protectRoutes(testRoutes);
  const router = createMemoryRouter(protectedRoutes, {
    initialEntries: [initialRoute],
  });

  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}

describe('@hilla/react-auth', () => {
  describe('protectRoutes', () => {
    async function testRoute(route: string, user: User | undefined, canAccess: boolean) {
      const result = render(<TestApp initialRoute={route} user={user} />);
      const expectedText = canAccess ? `route: ${route}` : 'route: /login';
      await waitFor(() => expect(result.getByText(expectedText)).to.exist);
      result.unmount();
    }

    it('should protect routes when no user is authenticated', async () => {
      await testRoute('/public', undefined, true);
      await testRoute('/protected/login', undefined, false);
      await testRoute('/protected/role/user', undefined, false);
      await testRoute('/protected/role/admin', undefined, false);
      await testRoute('/protected/roleonly/user', undefined, false);
      await testRoute('/protected/roleonly/admin', undefined, false);
    });

    it('should protect routes when user without roles is authenticated', async () => {
      const user = { name: 'John', roles: [] };
      await testRoute('/public', user, true);
      await testRoute('/protected/login', user, true);
      await testRoute('/protected/role/user', user, false);
      await testRoute('/protected/role/admin', user, false);
      await testRoute('/protected/roleonly/user', user, false);
      await testRoute('/protected/roleonly/admin', user, false);
    });

    it('should protect routes when user with user role is authenticated', async () => {
      const user = { name: 'John', roles: ['user'] };
      await testRoute('/public', user, true);
      await testRoute('/protected/login', user, true);
      await testRoute('/protected/role/user', user, true);
      await testRoute('/protected/role/admin', user, false);
      await testRoute('/protected/roleonly/user', user, true);
      await testRoute('/protected/roleonly/admin', user, false);
    });

    it('should protect routes when user with all roles is authenticated', async () => {
      const user = { name: 'John', roles: ['user', 'admin'] };
      await testRoute('/public', user, true);
      await testRoute('/protected/login', user, true);
      await testRoute('/protected/role/user', user, true);
      await testRoute('/protected/role/admin', user, true);
      await testRoute('/protected/roleonly/user', user, true);
      await testRoute('/protected/roleonly/admin', user, true);
    });
  });
});
