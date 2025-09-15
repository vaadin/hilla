import type User from 'Frontend/generated/com/example/application/data/entity/User.js';
import type Role from 'Frontend/generated/com/example/application/data/Role.js';
import { UserEndpoint } from 'Frontend/generated/endpoints.js';
import { createContext, Dispatch, useEffect, useReducer } from 'react';

export type AuthenticateThunk = () => Promise<void>;
export type UnauthenticateThunk = () => void;

const LOGIN_FETCH = 'LOGIN_FETCH';
const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
const LOGIN_FAILURE = 'LOGIN_FAILURE';
const LOGOUT = 'LOGOUT';

export type AuthUser = User &
  Readonly<{
    profilePictureUrl: string;
  }>;

type AuthState = Readonly<{
  initializing: boolean;
  loading: boolean;
  user?: AuthUser;
  error?: string;
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

type LoginActions = LoginFetchAction | LoginSuccessAction | LoginFailureAction;

type LogoutAction = Readonly<{
  type: typeof LOGOUT;
}>;

function createAuthenticateThunk(dispatch: Dispatch<LoginActions>) {
  async function authenticate() {
    dispatch({ type: LOGIN_FETCH });

    // Get user info from endpoint
    const userInfo = await UserEndpoint.getAuthenticatedUser();
    if (userInfo) {
      const profilePictureUrl = `data:image;base64,0`;
      const user = {
        ...userInfo,
        profilePictureUrl,
      };

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
  function logout() {
    dispatch({ type: LOGOUT });
  }

  return logout;
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
  rolesAllowed?: readonly Role[];
}>;

export type Authentication = Readonly<{
  state: AuthState;
  authenticate: AuthenticateThunk;
  unauthenticate: UnauthenticateThunk;
  hasAccess: (access?: AccessProps) => boolean;
}>;

export function useAuth(): Authentication {
  const [state, dispatch] = useReducer(reducer, initialState);
  const authenticate = createAuthenticateThunk(dispatch);
  const unauthenticate = createUnauthenticateThunk(dispatch);

  useEffect(() => {
    authenticate();
  }, []);

  return {
    state,
    authenticate,
    unauthenticate,
    hasAccess(access?: AccessProps): boolean {
      const requiresAuth = access?.requiresLogin || access?.rolesAllowed;
      if (!requiresAuth) {
        return true;
      }

      if (!state.user) {
        return false;
      }

      if (access.rolesAllowed) {
        // @ts-ignore
        return access.rolesAllowed.some((allowedRole) => state.user!.roles.includes(allowedRole));
      }

      return true;
    },
  };
}

export const AuthContext = createContext<Authentication>({
  state: initialState,
  async authenticate() {},
  unauthenticate() {},
  hasAccess(access?: AccessProps): boolean {
    return !access?.requiresLogin && !access?.rolesAllowed;
  },
});
