import { expect } from '@esm-bundle/chai';
import { render, waitFor } from '@testing-library/react';
import { configureAuth } from '../src';

interface CustomUser {
  name: string;
  hasRole(role: string): boolean;
}

let user: CustomUser | undefined;
const getAuthenticatedUser = async () => Promise.resolve(user);
const { AuthProvider, useAuth } = configureAuth(getAuthenticatedUser, (role) => (user ? user.hasRole(role) : false));

function TestComponent() {
  const { hasAccess, state } = useAuth();
  return <div>{hasAccess({ rolesAllowed: ['admin'] }) ? state.user?.name : 'Not an admin'}</div>;
}

function TestApp() {
  return (
    <AuthProvider>
      <TestComponent />
    </AuthProvider>
  );
}

describe('@hilla/react-auth', () => {
  describe('useAuth', () => {
    it('should have access when the role matches', async () => {
      user = { name: 'John', hasRole: (_) => true };
      const { getByText } = render(<TestApp />);
      await waitFor(() => expect(getByText('John')).to.exist);
    });

    it('should not have access when the role does not match', async () => {
      user = { name: 'John', hasRole: (_) => false };
      const { getByText } = render(<TestApp />);
      await waitFor(() => expect(getByText('Not an admin')).to.exist);
    });

    it('should not have access when not logged in', async () => {
      user = undefined;
      const { getByText } = render(<TestApp />);
      await waitFor(() => expect(getByText('Not an admin')).to.exist);
    });
  });
});
