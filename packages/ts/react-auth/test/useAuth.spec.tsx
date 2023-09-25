import { expect } from '@esm-bundle/chai';
import { render, waitFor } from '@testing-library/react';
import { configureAuth } from '../src';

interface CustomUser {
  name: string;
}

interface CustomUserWithRoles {
  name: string;
  roles: string[];
}

let user: CustomUser | undefined;
const getAuthenticatedUser = async () => Promise.resolve(user);
const { AuthProvider, useAuth } = configureAuth(getAuthenticatedUser);

function TestComponent() {
  const auth = useAuth();
  return <div>{auth.state.user ? auth.state.user.name : 'Not logged in'}</div>;
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
    it('should be able to access user information after login', async () => {
      user = { name: 'John' };
      const { getByText } = render(<TestApp />);
      await waitFor(() => expect(getByText('John')).to.exist);
    });

    it('should not be able to access user information after login', async () => {
      user = undefined;
      const { getByText } = render(<TestApp />);
      await waitFor(() => expect(getByText('Not logged in')).to.exist);
    });
  });
});
