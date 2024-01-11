import { login as _login, logout as _logout } from '@vaadin/hilla-core';
import { AuthenticateThunk, UnauthenticateThunk } from 'Frontend/useAuth.js';

export async function login(username: string, password: string, authenticate: AuthenticateThunk) {
  const result = await _login(username, password);

  if (!result.error) {
    await authenticate();
  }

  return result;
}

export async function logout(unauthenticate: UnauthenticateThunk) {
  await _logout();
  unauthenticate();
}
