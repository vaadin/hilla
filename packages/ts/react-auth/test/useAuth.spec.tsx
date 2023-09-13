import { expect } from '@esm-bundle/chai';
import { render, waitFor } from '@testing-library/react';
import { AuthContext, useAuth } from '../src';

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
});
