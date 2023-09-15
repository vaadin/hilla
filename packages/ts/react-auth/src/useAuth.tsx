import { login as _login, logout as _logout, type LoginResult } from '@hilla/frontend';
import { createContext, type Dispatch, useEffect, useReducer } from 'react';

type LoginFunction = (username: string, password: string) => Promise<LoginResult>;
type LogoutFunction = () => Promise<void>;

const LOGIN_FETCH = 'LOGIN_FETCH';
const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
const LOGIN_FAILURE = 'LOGIN_FAILURE';
const LOGOUT = 'LOGOUT';

/**
 * The user object returned from the authentication provider.
 * The properties are the same as the ones returned from the
 * {@link https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims | OpenID Connect Standard Claims}
 * specification, with the addition of the `roles` property.
 *
 * The user is not required to comply with this format. This is just for convenience.
 */
export type AuthUser = Readonly<{
  sub?: string;
  name?: string;
  givenName?: string;
  familyName?: string;
  middleName?: string;
  nickName?: string;
  preferredUsername?: string;
  profile?: string;
  picture?: string;
  website?: string;
  email?: string;
  emailVerified?: boolean;
  gender?: string;
  birthdate?: string;
  zoneinfo?: string;
  locale?: string;
  phoneNumber?: string;
  phoneNumberVerified?: boolean;
  roles?: string[];
}>;

/**
 * The type of the function that is used to get the authenticated user.
 */
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

/**
 * The properties that can be used to control access to a route.
 * They can be added to the route type handler as properties.
 */
export type AccessProps = Readonly<{
  requiresLogin?: boolean;
  rolesAllowed?: readonly string[];
}>;

/**
 * The type of the authentication hook.
 */
export type Authentication = Readonly<{
  state: AuthState;
  login: LoginFunction;
  logout: LogoutFunction;
  hasAccess(accessProps: AccessProps): boolean;
}>;

/**
 * The hook that can be used to authenticate the user.
 * It returns the state of the authentication and the functions
 * to authenticate and unauthenticate the user.
 */
export function useAuth(getAuthenticatedUser?: AuthFunctionType): Authentication {
  const [state, dispatch] = useReducer(reducer, initialState);
  const authenticate = createAuthenticateThunk(
    dispatch,
    getAuthenticatedUser ?? (async () => Promise.resolve(undefined)),
  );
  const unauthenticate = createUnauthenticateThunk(dispatch);

  async function login(username: string, password: string): Promise<LoginResult> {
    const result = await _login(username, password);

    if (!result.error) {
      await authenticate();
    }

    return result;
  }

  async function logout(): Promise<void> {
    await _logout();
    unauthenticate();
  }

  useEffect(() => {
    authenticate().catch(() => {
      // Do nothing
    });
  }, []);

  return {
    state,
    login,
    logout,
    hasAccess(accessProps: AccessProps): boolean {
      const requiresAuth = accessProps.requiresLogin ?? accessProps.rolesAllowed;
      if (!requiresAuth) {
        return true;
      }

      if (!state.user) {
        return false;
      }

      if (accessProps.rolesAllowed) {
        return accessProps.rolesAllowed.some((allowedRole) => state.user?.roles?.includes(allowedRole));
      }

      return true;
    },
  };
}

/**
 * The hook that can be used to get the authentication state.
 * It returns the state of the authentication.
 */
export const AuthContext = createContext<Authentication>({
  state: initialState,
  async login() {
    throw new Error('AuthContext not initialized');
  },
  async logout() {
    throw new Error('AuthContext not initialized');
  },
  hasAccess(): boolean {
    throw new Error('AuthContext not initialized');
  },
});
