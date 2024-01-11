import { expect } from '@esm-bundle/chai';
import { renderHook, waitFor } from '@testing-library/react';
import { configureAuth } from '../src';

describe('@vaadin/react-auth', () => {
  describe('useAuth', () => {
    it('should provide user in state', async () => {
      const user = { customRoles: ['admin'] };
      const { AuthProvider, useAuth } = configureAuth(async () => Promise.resolve(user));
      const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

      await waitFor(() => expect(result.current.state.user).to.equal(user));
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
