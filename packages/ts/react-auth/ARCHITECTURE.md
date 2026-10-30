# React authentication state

## Overview

`@vaadin/hilla-react-auth` keeps track of who is logged in, for the React
part of an application, and guards routes by it. It doesn't authenticate
anyone itself: Spring Security does that on the server, and the `login` and
`logout` helpers of `@vaadin/hilla-frontend` talk to it (see
[`../frontend/ARCHITECTURE.md`](../frontend/ARCHITECTURE.md#login-and-logout)).
This package asks an endpoint who the user is, keeps the answer in a React
context, and lets routes and components check it.

An application configures it once, with an endpoint method that returns the
current user:

```tsx
export const { AuthProvider, useAuth } = configureAuth(UserInfoService.getUserInfo);
```

It puts `AuthProvider` around the router, calls `useAuth()` in components,
and turns on route protection with `RouterConfigurationBuilder.protect()` in
`@vaadin/hilla-file-router`, or with `protectRoutes` for hand-written routes.

## Design goals

- **The server is the source of truth.** The user comes from an endpoint
  call, never from a cookie or a token parsed in the browser. That works the
  same for session-based and stateless (JWT) authentication, and for any
  user type the application defines.
- **Bring your own user type.** `configureAuth` is generic over the user, and
  the roles are read with `getRoles`, by default from `user.roles`.
- **Route guards are client-side UX, not security.** They decide what to show
  and where to redirect. The server still checks every endpoint call and every
  server route.

## State

`AuthProvider` keeps the state in a `useReducer` and publishes it through
`AuthContext`:

- `initializing` is `true` until the first check has started, and `loading`
  is `true` while a check runs. `ProtectedRoute` renders an empty element in
  both cases, so a protected view is not shown before the user is known.
- `user` is the endpoint's answer, and `error` is set when there is no user.

On mount, the provider calls `getAuthenticatedUser` once. An
`UnauthorizedResponseError`, a `401` from a protected endpoint, means "not
logged in", not a failure (#2242). That allows the user endpoint to require
authentication instead of returning `null` for anonymous users.

## Login and logout

`useAuth().login` calls `login` from `@vaadin/hilla-frontend` and, when it
succeeds, asks the endpoint again for the user. `logout` calls `logout` and
clears the user. Neither changes the state on its own when the request fails,
so the context always reflects what the server last said.

The `login` helper reloads the page by default after a successful login,
which runs the initial check again. With `navigate` or `onSuccess` set, it
doesn't reload, and the explicit re-check is what updates the state.

## Access checks

`hasAccess({ loginRequired, rolesAllowed })` is the single check that routes
and components share:

- No `loginRequired` (or the deprecated `requiresLogin`) and no
  `rolesAllowed` means public.
- Otherwise a user is required, and with `rolesAllowed`, one of the user's
  roles has to be in the list.

`protectRoute` / `protectRoutes` ([`src/ProtectedRoute.tsx`](src/ProtectedRoute.tsx))
read these properties from a route's `handle`, which is where the file router
puts a view's `ViewConfig`. They wrap the route's element in `ProtectedRoute`,
which redirects to the login path (`/login` by default) with the original
location in the navigation state. The file router's `protect()` applies
`protectRoute` to every route it builds.

## Working on it

- Unit tests: [`test/useAuth.spec.tsx`](test/useAuth.spec.tsx) and
  [`test/ProtectedRoute.spec.tsx`](test/ProtectedRoute.spec.tsx).
- Integration tests: `packages/java/tests/spring/security` and
  `packages/java/tests/spring/security-jwt`.
- The server side of the same checks (`RouteUtil` for client views,
  `@RolesAllowed` and the other annotations on endpoints) is described in the
  [file router note](../file-router/ARCHITECTURE.md) and in
  [`guidelines/architecture.md`](../../../guidelines/architecture.md).
