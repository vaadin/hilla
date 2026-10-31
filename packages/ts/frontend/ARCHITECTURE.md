# Endpoint calls and push

## Overview

A browser callable method is reached in one of two ways. A method that returns
one value is a single HTTP `POST`, made by `ConnectClient.call`. A method that
returns a `Flux` or an `EndpointSubscription` is a subscription: its values
are pushed over one WebSocket connection, `FluxConnection`, which
`ConnectClient.subscribe` opens on first use.

The generated endpoint functions only name the endpoint, the method and the
parameters; the generated `connect-client.default.ts` holds the one
`ConnectClient` they share. This note follows a call, then a subscription,
through the client and the server, and ends with login and logout. The server
side of a call (`EndpointController`, `EndpointInvoker`) is described in
[Calling an endpoint](../../../guidelines/architecture.md#calling-an-endpoint),
and the mapping between `null` and `undefined` in
[Nullability](../../../guidelines/architecture.md#nullability).

## Design goals

- **Thin generated functions.** CSRF, errors, connection state and file upload
  live in `ConnectClient` and its middleware chain, not in the generated code.
  An application changes them by adding middleware, not by regenerating.
- **One connection for all subscriptions.** Subscriptions are multiplexed over
  a single WebSocket by an id, and the WebSocket is opened only when the first
  subscription is made. An application that never subscribes never opens it.
- **Server state does not survive a lost connection.** When the connection
  drops, the server disposes every subscription on it. Whether one is made
  again after reconnecting is the application's decision.
- **Server errors stay on the server.** An exception in a `Flux` reaches the
  client as a generic message; the details are only logged.

## Calling a method

`ConnectClient.call` ([src/Connect.ts](src/Connect.ts)) builds a `Request`,
runs it through the middleware chain, and returns the parsed response.

### CSRF

Every call waits for the CSRF info from `csrfInfoSource`
([src/CsrfInfoSource.ts](src/CsrfInfoSource.ts)) and sends its header
entries. There are two kinds:

- `Spring`, when Spring Security's token is in the `XSRF-TOKEN` cookie or the
  `_csrf` meta tags of the page; the header name comes from `_csrf_header`.
  Flow writes the meta tags into `index.html` (its `SpringCsrfTokenUtil`), and
  the cookie is set by Spring's `CookieCsrfTokenRepository`, which stateless
  authentication uses.
- `Vaadin` otherwise: the `csrfToken` cookie, sent as `X-CSRF-Token`.
  `CsrfIndexHtmlRequestListener` sets the cookie on the `index.html` response
  when Spring provides no token. The server's `CsrfChecker` compares the two
  (a double-submit cookie), and answers `401` when they differ.

In a page, `BrowserCsrfInfoSource` reads the info from the document. In a
worker there is no document, so `SharedCsrfInfoSource` asks the pages for it
over a `BroadcastChannel`; a page answers, and also broadcasts the info
whenever it resets. `reset()` re-reads the info, and is what login and logout
call when the token changes.

This is what lets a service worker call endpoints (#3506). The client modules
avoid DOM globals so that they can be bundled into the service worker (#3438),
and Flow's service worker build defines `globalThis.document` as `undefined`,
so the worker always picks `SharedCsrfInfoSource`. The two channels are
`@vaadin/hilla-frontend/SharedCsrfUtils.update` and `.requestUpdate`. Each
info carries a `timestamp`, and a receiver ignores info older than what it
has, so a late answer from a stale tab doesn't overwrite a newer token. A
page closes its channels on `pagehide` and asks for an update on `pageshow`,
for when it comes back from the back/forward cache.

### Request body

The parameters are sent as a JSON object by name, with `undefined` written as
`null`. When a parameter contains a `File`, the request is multipart instead:
the files are taken out of the object, each into a part named by its JSON
pointer (`/orders/1/invoice`), and the rest goes into the `hilla_body_part`
part (`BODY_PART_NAME`). `EndpointController` puts the files back at those
pointers before invoking the method.

### Middleware

The chain is folded with `reduceRight`, so it runs in this order:

1. `responseHandlerMiddleware`, internal and always first, so it is the last
   to see the response. It turns a non-OK response into an error (below) and
   parses the body, with `null` read as `undefined`. It returns the parsed
   value, not a `Response`.
2. The application's `middlewares`, in order. They see the `Request` on the
   way in and the raw `Response` on the way out, and may replace either or
   call `next` again, as `InvalidSessionMiddleware` does to retry after a
   login.
3. `fetchNext`, which calls `fetch` and reports to Flow's
   `window.Vaadin.connectionState`: loading started, finished, or failed. An
   aborted request counts as finished. A call with `mute: true` reports
   nothing.

A non-OK response becomes, in order of precedence: an `EndpointValidationError`
when the body has `validationErrorData`; an `EndpointError` when it has a
`type` (an `EndpointException` thrown on the server); an
`UnauthorizedResponseError` or `ForbiddenResponseError` for `401` and `403`;
an `EndpointResponseError` otherwise ([src/EndpointErrors.ts](src/EndpointErrors.ts)).

## Subscribing

`ConnectClient.subscribe` hands the call to `FluxConnection`
([src/FluxConnection.ts](src/FluxConnection.ts)). The parameters are sent as an
array, in the order of the generated function's parameter object; the server
binds them to the method's parameters by position.

### The connection

`FluxConnection` connects with Atmosphere over a WebSocket only, to
`HILLA/push` under the call prefix with a trailing `connect` removed (the
generated client's prefix `connect` gives `HILLA/push`, relative to the
document base). The CSRF headers are read once, when the connection opens.
Atmosphere reconnects on its own every 5 seconds, practically forever.
Outside a browser (`VAADIN_BROWSER_ENVIRONMENT`), Atmosphere is not loaded and
no connection is made.

The connection `State` is `ACTIVE`, `RECONNECTING` or `INACTIVE`, and a
`state-changed` event reports when it becomes active or inactive. A message
sent while the state is `INACTIVE`, or before the socket exists, is queued and
sent when the connection opens.

### Messages

The messages ([src/FluxMessages.ts](src/FluxMessages.ts)) are JSON with an
`@type` and the subscription `id`, a counter local to the connection:

- To the server: `subscribe` (endpoint, method, parameters) and `unsubscribe`.
- To the client: `update` (one value, as `item`), `complete` and `error` (a
  message string).

The server has the same messages as Jackson subtypes, in `push/messages`.
Pushed values go through the endpoint's object mapper but, unlike call
responses, `null` is not mapped to `undefined` on the client.

### A subscription's life

Each subscription has a `FluxSubscriptionState`, reported to
`onConnectionStateChange`:

- `CONNECTING` when it is made, and again for every subscription while the
  connection is reconnecting. There is no acknowledgement of a `subscribe`:
  the state becomes `CONNECTED` with the first `update`.
- `CLOSED` on `complete`, `error` or `cancel()`, after which the subscription
  and its callbacks are forgotten. An `error` without an `onError` callback is
  thrown from the message handler.
- `CLOSED` also when Atmosphere gives up reconnecting, but then the
  subscription is kept.

`context(host)` cancels the subscription when a Lit host disconnects.

When the connection closes and opens again, the server has already disposed
the subscriptions of the old one. For each subscription, the client calls its
`onSubscriptionLost` callback: `RESUBSCRIBE` sends the same `subscribe`
message again under the same id; anything else, or no callback, closes it.

## The server side of push

The push code is in
[com/vaadin/hilla/push](../../java/endpoint/src/main/java/com/vaadin/hilla/push).

- `PushConfigurer` registers an `AtmosphereServlet` at `/HILLA/push`, under the
  endpoint prefix with its trailing `connect` removed, the same rule the
  client uses. It overrides the global Atmosphere mapping Flow sets.
- `PushEndpoint` is the Atmosphere handler. A `GET` opens a connection, a
  `POST` carries a message. It reads the message with the endpoint object
  mapper, and sets the Spring `SecurityContext` from the request's principal
  while the message is handled, so the access check sees the user. It writes
  replies to that one connection, not through a broadcaster. A disconnect, or
  an error on the connection, is passed on to the handler.
- `PushMessageHandler` keeps, per connection id (Atmosphere's UUID), the
  subscriptions by id: the `Disposable` of the `Flux` subscription and, for an
  `EndpointSubscription`, its unsubscribe callback.

On `subscribe`, the handler checks that the method returns a `Flux` or an
`EndpointSubscription` and calls `EndpointInvoker.invoke`, which applies the
same access check and parameter handling as a call. It then subscribes to the
`Flux`:

- Each item is sent as an `update`.
- On completion, `complete` is sent; on an error, `error` with the message
  `Exception in Flux`, and the exception is logged. Both remove the
  subscription first.
- A missing endpoint or method, a failed access check, or parameters that
  can't be read is an `error` with no `Flux` subscribed. An
  `EndpointException` (including a failed parameter validation) is not caught:
  the client gets no reply, and the subscription stays `CONNECTING`.
- A second `subscribe` with an id already in use is logged and ignored, with
  no reply, so the subscription using that id is not affected.

The unsubscribe callback of an `EndpointSubscription` runs on `unsubscribe`
and on disconnect, when the browser went away, not when the `Flux` completes
or fails.

Full-stack signals subscribe through the same channel, as a subscription to a
method of their own endpoint.

## Login and logout

[src/Authentication.ts](src/Authentication.ts) has helpers for Spring
Security's form login and logout. Both change the CSRF token, so both update
it before anything else is sent:

- `login` posts the form with a `source: typescript` header, so the server's
  success handler answers with `Result`, `Saved-url`, `Default-url` and the new
  Spring CSRF header and token in response headers. The client writes the
  token into the `_csrf` meta tags and calls `csrfInfoSource.reset()`. The
  server side of this contract is Flow's
  `VaadinSavedRequestAwareAuthenticationSuccessHandler`; a login is successful
  only with `Result: success`. After it, the client goes to `Saved-url`, then
  `Default-url`, then the base URL.
- `logout` uses a `fetch` when `navigate` or `onSuccess` is given, and reads
  the new token from the returned page; otherwise it submits a form and lets
  the browser navigate. When the logout fails, it retries with the token from
  a freshly loaded page, and as a last resort clears the token. It always
  removes the JWT cookie of stateless authentication.
- `InvalidSessionMiddleware` calls back on a `401`, and retries the call with
  the token the callback's login returned.

Both helpers reload the page by default afterwards. An application that
navigates without a reload keeps the open push connection, with the CSRF
headers it was opened with.

With stateless authentication (Flow's `VaadinStatelessSecurityConfigurer`),
the user is kept in a JWT split over two cookies, and the server re-issues it
on every response. The client never reads the token: who is logged in comes
from an endpoint, through `@vaadin/hilla-react-auth`
([`../react-auth/ARCHITECTURE.md`](../react-auth/ARCHITECTURE.md)). The only
place the client touches it is `logout`, which removes the JS-readable
`jwt.headerAndPayload` cookie even when the server could not be reached.
