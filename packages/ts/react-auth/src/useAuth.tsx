import { login as _login, logout as _logout, type LoginResult } from '@hilla/frontend';
import { createContext, type Dispatch, useEffect, useReducer, useContext } from 'react';

export type AuthenticateThunk = () => Promise<void>;
export type UnauthenticateThunk = () => void;

const LOGIN_FETCH = 'LOGIN_FETCH';
const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
const LOGIN_FAILURE = 'LOGIN_FAILURE';
const LOGOUT = 'LOGOUT';

export type AuthUser = Readonly<{
  birthdate?: string;
  email?: string;
  familyName?: string;
  fullName?: string;
  gender?: string;
  givenName?: string;
  locale?: string;
  middleName?: string;
  nickName?: string;
  phoneNumber?: string;
  picture?: string;
  preferredUsername?: string;
  roles?: string[];
}>;

export type AuthFunctionType = () => Promise<Partial<AuthUser> | undefined>;

type AuthState = Readonly<{
  initializing: boolean;
  loading: boolean;
  user?: AuthUser;
  error?: string;
  getAuthenticatedUser?: AuthFunctionType;
}>;

type LoginFetchAction = Readonly<{
  type: typeof LOGIN_FETCH;
}>;

type LoginSuccessAction = Readonly<{
  user: AuthUser;
  type: typeof LOGIN_SUCCESS;
}>;

type LoginFailureAction = Readonly<{
  error: string;
  type: typeof LOGIN_FAILURE;
}>;

type LoginActions = LoginFailureAction | LoginFetchAction | LoginSuccessAction;

type LogoutAction = Readonly<{
  type: typeof LOGOUT;
}>;

function createAuthenticateThunk(dispatch: Dispatch<LoginActions>, getAuthenticatedUser: AuthFunctionType) {
  async function authenticate() {
    dispatch({ type: LOGIN_FETCH });

    // Get user info from endpoint
    const user = await getAuthenticatedUser();
    if (user) {
      dispatch({
        user,
        type: LOGIN_SUCCESS,
      });
    } else {
      dispatch({
        error: 'Not authenticated',
        type: LOGIN_FAILURE,
      });
    }
  }

  return authenticate;
}

function createUnauthenticateThunk(dispatch: Dispatch<LogoutAction>) {
  return () => {
    dispatch({ type: LOGOUT });
  };
}

const initialState: AuthState = {
  initializing: true,
  loading: false,
};

function reducer(state: AuthState, action: LoginActions | LogoutAction) {
  switch (action.type) {
    case LOGIN_FETCH:
      return {
        initializing: false,
        loading: true,
      };
    case LOGIN_SUCCESS:
      return {
        initializing: false,
        loading: false,
        user: action.user,
      };
    case LOGIN_FAILURE:
      return {
        initializing: false,
        loading: false,
        error: action.error,
      };
    case LOGOUT:
      return { initializing: false, loading: false };
    default:
      return state;
  }
}

export type AccessProps = Readonly<{
  requiresLogin?: boolean;
  rolesAllowed?: readonly string[];
}>;

export type Authentication = Readonly<{
  state: AuthState;
  authenticate: AuthenticateThunk;
  unauthenticate: UnauthenticateThunk;
  hasAccess({ handle }: { handle?: AccessProps }): boolean;
}>;

export function useAuth(getAuthenticatedUser?: AuthFunctionType): Authentication {
  const [state, dispatch] = useReducer(reducer, initialState);
  const authenticate = createAuthenticateThunk(
    dispatch,
    getAuthenticatedUser ?? (async () => Promise.resolve(undefined)),
  );
  const unauthenticate = createUnauthenticateThunk(dispatch);

  useEffect(() => {
    authenticate().catch(() => {
      // Do nothing
    });
  }, []);

  return {
    state,
    authenticate,
    unauthenticate,
    hasAccess({ handle }: { handle?: AccessProps }): boolean {
      const requiresAuth = handle?.requiresLogin ?? handle?.rolesAllowed;
      if (!requiresAuth) {
        return true;
      }

      if (!state.user) {
        return false;
      }

      if (handle?.rolesAllowed) {
        return handle.rolesAllowed.some((allowedRole) => state.user?.roles?.includes(allowedRole));
      }

      return true;
    },
  };
}

export const AuthContext = createContext<Authentication>({
  state: initialState,
  async authenticate() {},
  unauthenticate() {},
  hasAccess({ handle }: { handle?: AccessProps }): boolean {
    return !handle?.requiresLogin && !handle?.rolesAllowed;
  },
});

export async function login(username: string, password: string, authenticate: AuthenticateThunk): Promise<LoginResult> {
  const result = await _login(username, password);

  if (!result.error) {
    await authenticate();
  }

  return result;
}

export async function logout(unauthenticate: UnauthenticateThunk): Promise<void> {
  await _logout();
  unauthenticate();
}
