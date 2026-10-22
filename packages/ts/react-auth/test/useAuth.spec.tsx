import { render, renderHook, waitFor } from '@testing-library/react';
import { UnauthorizedResponseError } from '@vaadin/hilla-frontend';
import { StrictMode } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { configureAuth } from '../src/index.js';

describe('@vaadin/react-auth', () => {
  describe('useAuth', () => {
    it('should provide user in state', async () => {
      const user = { customRoles: ['admin'] };
      const { AuthProvider, useAuth } = configureAuth(async () => Promise.resolve(user));
      const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

      await waitFor(() => expect(result.current.state.user).to.equal(user));
    });

    it('should fetch the authenticated user once per mount in Strict Mode', async () => {
      const user = { customRoles: ['admin'] };
      const getAuthenticatedUser = vi.fn(async () => {
        await Promise.resolve();
        return user;
      });
      const { AuthProvider, useAuth } = configureAuth(getAuthenticatedUser);
      const AuthState = () => <span>{useAuth().state.user ? 'authenticated' : 'unauthenticated'}</span>;

      const { container, unmount } = render(
        <StrictMode>
          <AuthProvider>
            <AuthState />
          </AuthProvider>
        </StrictMode>,
      );

      expect(getAuthenticatedUser).toHaveBeenCalledOnce();
      await waitFor(() => expect(container.textContent).to.equal('authenticated'));

      unmount();
      const { container: remountedContainer } = render(
        <StrictMode>
          <AuthProvider>
            <AuthState />
          </AuthProvider>
        </StrictMode>,
      );

      expect(getAuthenticatedUser).toHaveBeenCalledTimes(2);
      await waitFor(() => expect(remountedContainer.textContent).to.equal('authenticated'));
    });

    it('should handle 401 from UserInfo endpoint', async () => {
      const error401Response = new Response('Unauthorized', { status: 401, statusText: 'Unauthorized' });
      const error401 = new UnauthorizedResponseError('Not authorized', error401Response);
      const { AuthProvider, useAuth } = configureAuth(async () => Promise.reject(error401));
      const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

      await waitFor(() => expect(result.current.state).to.include({ loading: false, initializing: false }));
      expect(result.current.state.user).to.be.undefined;
    });

    describe('hasAccess', () => {
      it('should not give access if user has no roles', async () => {
        const user = {};
        const { AuthProvider, useAuth } = configureAuth(async () => Promise.resolve(user));

        const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

        await waitFor(() => expect(result.current.state.user).to.equal(user));
        expect(result.current.hasAccess({ rolesAllowed: ['admin'] })).to.be.false;
        expect(result.current.hasAccess({ rolesAllowed: ['superadmin'] })).to.be.false;
      });

      it('should handle incompatible roles property', async () => {
        const user = { roles: 'admin' };
        const { AuthProvider, useAuth } = configureAuth(async () => Promise.resolve(user));

        const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

        await waitFor(() => expect(result.current.state.user).to.equal(user));
        expect(result.current.hasAccess({ rolesAllowed: ['admin'] })).to.be.false;
        expect(result.current.hasAccess({ rolesAllowed: ['superadmin'] })).to.be.false;
      });

      it('should use roles property by convention', async () => {
        const user = { roles: ['admin'] };
        const { AuthProvider, useAuth } = configureAuth(async () => Promise.resolve(user));
        const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

        await waitFor(() => expect(result.current.state.user).to.equal(user));
        expect(result.current.hasAccess({ rolesAllowed: ['admin'] })).to.be.true;
        expect(result.current.hasAccess({ rolesAllowed: ['superadmin'] })).to.be.false;
      });

      it('should use custom roles accessor when configured', async () => {
        const user = { roles: ['superadmin'], customRoles: ['admin'] };
        const { AuthProvider, useAuth } = configureAuth(async () => Promise.resolve(user), {
          getRoles: (authenticatedUser) => authenticatedUser.customRoles,
        });
        const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

        await waitFor(() => expect(result.current.state.user).to.equal(user));
        expect(result.current.hasAccess({ rolesAllowed: ['admin'] })).to.be.true;
        expect(result.current.hasAccess({ rolesAllowed: ['superadmin'] })).to.be.false;
      });
    });
  });
});
