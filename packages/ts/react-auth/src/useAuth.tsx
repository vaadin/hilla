import { login as _login, logout as _logout, type LoginResult } from '@hilla/frontend';
import { createContext, type Dispatch, type ReactNode, useContext, useEffect, useReducer } from 'react';

type LoginFunction = (username: string, password: string) => Promise<LoginResult>;
type LogoutFunction = () => Promise<void>;

const LOGIN_FETCH = 'LOGIN_FETCH';
const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
const LOGIN_FAILURE = 'LOGIN_FAILURE';
const LOGOUT = 'LOGOUT';

/**
 * The type of the function that is used to get the authenticated user.
 */
export type GetUserFn<TUser> = () => Promise<TUser | undefined>;

export type UserInRoleFn = (role: string) => boolean;

type AuthState<TUser> = Readonly<{
  initializing: boolean;
  loading: boolean;
  user?: TUser;
  error?: string;
  getAuthenticatedUser?: GetUserFn<TUser>;
}>;

type LoginFetchAction = Readonly<{
  type: typeof LOGIN_FETCH;
}>;

type LoginSuccessAction = Readonly<{
  user: unknown;
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

function createAuthenticateThunk(dispatch: Dispatch<LoginActions>, getAuthenticatedUser: GetUserFn<unknown>) {
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

const initialState: AuthState<never> = {
  initializing: true,
  loading: false,
};

function reducer(state: AuthState<unknown>, action: LoginActions | LogoutAction) {
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
export type Authentication<TUser> = Readonly<{
  state: AuthState<TUser>;
  login: LoginFunction;
  logout: LogoutFunction;
  hasAccess(accessProps: AccessProps): boolean;
}>;

/**
 * The hook that can be used to get the authentication state.
 * It returns the state of the authentication.
 */
export const AuthContext = createContext<Authentication<unknown>>({
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

interface AuthProviderProps<TUser> extends React.PropsWithChildren {
  getAuthenticatedUser: GetUserFn<TUser>;
  isUserInRole?: UserInRoleFn;
}

function AuthProvider<TUser>({ children, getAuthenticatedUser, isUserInRole }: AuthProviderProps<TUser>) {
  const [state, dispatch] = useReducer(reducer, initialState);
  const authenticate = createAuthenticateThunk(dispatch, getAuthenticatedUser);
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

  type HasRoles = Readonly<{
    roles: string[];
  }>;

  function hasAccess(accessProps: AccessProps): boolean {
    const requiresAuth = accessProps.requiresLogin ?? accessProps.rolesAllowed;
    if (!requiresAuth) {
      return true;
    }

    if (!state.user) {
      return false;
    }

    if (accessProps.rolesAllowed) {
      return accessProps.rolesAllowed.some(
        isUserInRole ?? ((role) => state.user && (state.user as HasRoles).roles.includes(role)),
      );
    }

    return true;
  }

  useEffect(() => {
    authenticate().catch(() => {
      // Do nothing
    });
  }, []);

  const auth = {
    state,
    login,
    logout,
    hasAccess,
  };

  return <AuthContext.Provider value={auth}>{children}</AuthContext.Provider>;
}

export type AuthHook<TUser> = () => Authentication<TUser>;

/**
 * The hook that can be used to authenticate the user.
 * It returns the state of the authentication and the functions
 * to authenticate and unauthenticate the user.
 */
function useAuth(): Authentication<unknown> {
  return useContext(AuthContext);
}

interface AuthModule<TUser> {
  AuthProvider: React.FC<React.PropsWithChildren>;
  useAuth: AuthHook<TUser>;
}

export function configureAuth<TUser>(
  getAuthenticatedUser: GetUserFn<TUser>,
  isUserInRole?: UserInRoleFn,
): AuthModule<TUser> {
  function PreconfiguredAuthProvider({ children }: React.PropsWithChildren) {
    return (
      <AuthProvider<TUser> getAuthenticatedUser={getAuthenticatedUser} isUserInRole={isUserInRole}>
        {children}
      </AuthProvider>
    );
  }

  return {
    AuthProvider: PreconfiguredAuthProvider,
    useAuth: useAuth as AuthHook<TUser>,
  };
}
