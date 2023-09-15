import { expect } from '@esm-bundle/chai';
import { render, waitFor } from '@testing-library/react';
import { RouterProvider, createMemoryRouter } from 'react-router-dom';
import { AuthContext, type AuthUser, protectRoutes, type RouteObjectWithAuth, useAuth } from '../src';

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
      requiresLogin: true,
    },
  },
  {
    path: '/protected/role/user',
    element: <TestView route="/protected/role/user" />,
    handle: {
      requiresLogin: true,
      rolesAllowed: ['user'],
    },
  },
  {
    path: '/protected/role/admin',
    element: <TestView route="/protected/role/admin" />,
    handle: {
      requiresLogin: true,
      rolesAllowed: ['admin'],
    },
  },
];

function TestApp({ user, initialRoute }: { user?: AuthUser; initialRoute: string }) {
  const auth = useAuth(async () => Promise.resolve(user));
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  (auth.state as any).user = user;
  const protectedRoutes = protectRoutes(testRoutes);
  const router = createMemoryRouter(protectedRoutes, {
    initialEntries: [initialRoute],
  });

  return (
    <AuthContext.Provider value={auth}>
      <RouterProvider router={router} />
    </AuthContext.Provider>
  );
}

function SuccessfulLoginComponent() {
  const getAuthenticatedUser = async () => Promise.resolve({ name: 'John', roles: ['admin'] });
  const auth = useAuth(getAuthenticatedUser);

  return (
    <AuthContext.Provider value={auth}>
      <div>{auth.state.user?.name}</div>
    </AuthContext.Provider>
  );
}

function FailedLoginComponent() {
  const getAuthenticatedUser = async () => Promise.resolve(undefined);
  const auth = useAuth(getAuthenticatedUser);

  return (
    <AuthContext.Provider value={auth}>
      <div>{auth.state.user ? auth.state.user.name : 'Not logged in'}</div>
    </AuthContext.Provider>
  );
}

describe('@hilla/react-auth', () => {
  describe('useAuth', () => {
    it('should be able to access user information after login', async () => {
      const { getByText } = render(<SuccessfulLoginComponent />);
      await waitFor(() => expect(getByText('John')).to.exist);
    });

    it('should not be able to access user information after login', async () => {
      const { getByText } = render(<FailedLoginComponent />);
      await waitFor(() => expect(getByText('Not logged in')).to.exist);
    });
  });

  describe('protectRoutes', () => {
    function testRoute(route: string, user: AuthUser | undefined, canAccess: boolean) {
      const result = render(<TestApp initialRoute={route} user={user} />);
      if (canAccess) {
        expect(result.getByText(`route: ${route}`)).to.exist;
      } else {
        expect(result.getByText('route: /login')).to.exist;
      }
      result.unmount();
    }

    it('should protect routes when no user is authenticated', async () => {
      testRoute('/public', undefined, true);
      testRoute('/protected/login', undefined, false);
      testRoute('/protected/role/user', undefined, false);
      testRoute('/protected/role/admin', undefined, false);
    });

    it('should protect routes when user without roles is authenticated', async () => {
      const user = { name: 'John' };
      testRoute('/public', user, true);
      testRoute('/protected/login', user, true);
      testRoute('/protected/role/user', user, false);
      testRoute('/protected/role/admin', user, false);
    });

    it('should protect routes when user with user role is authenticated', async () => {
      const user = { name: 'John', roles: ['user'] };
      testRoute('/public', user, true);
      testRoute('/protected/login', user, true);
      testRoute('/protected/role/user', user, true);
      testRoute('/protected/role/admin', user, false);
    });

    it('should protect routes when user with all roles is authenticated', async () => {
      const user = { name: 'John', roles: ['user', 'admin'] };
      testRoute('/public', user, true);
      testRoute('/protected/login', user, true);
      testRoute('/protected/role/user', user, true);
      testRoute('/protected/role/admin', user, true);
    });
  });
});
