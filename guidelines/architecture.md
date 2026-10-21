# Architecture

How the core pieces of Hilla fit together, and what to keep in mind when
touching them.

## Calling an endpoint

A browser callable class is a Spring bean annotated with `@BrowserCallable`
(or the older `@Endpoint`). `@EndpointExposed` marks a superclass whose public
methods are exposed through the endpoints that extend it.

- `EndpointRegistry` discovers the browser callable beans at startup and
  registers their public methods. `EndpointNameChecker` rejects names that
  are reserved words in ECMAScript and TypeScript, since every endpoint and
  method name becomes a TypeScript identifier.
- `EndpointController` receives the call as a `POST` to
  `{prefix}/{endpoint}/{method}` (the prefix is `vaadin.endpoint.prefix`,
  `/connect` by default), as JSON or as multipart when files are sent.
- `EndpointInvoker` checks access (`@AnonymousAllowed`, `@PermitAll`,
  `@RolesAllowed`, `@DenyAll`), deserializes the parameters with Jackson,
  validates them with Bean Validation, invokes the method and serializes the
  result.
- On the client, the generated endpoint functions call `ConnectClient` from
  `@vaadin/hilla-frontend`, which handles CSRF, middleware and errors. An
  `EndpointException` thrown on the server reaches the client as an
  `EndpointError`, and a validation failure as an `EndpointValidationError`.

## Generating the TypeScript

The TypeScript of the endpoints is generated in Java, by
`packages/java/typescript-generator`:

1. The parser walks the browser callable classes with ClassGraph, and its
   plugins say what they find: the endpoints, their methods and the types
   they send (`backbone`), nullability (`nonnull`), what the validation and
   persistence annotations say about a value (`model`), polymorphic types
   (`subtypes`) and mapped types (`transfertypes`).
2. A plugin collects what the writers need into the generator model
   (`com.vaadin.hilla.generator.model`) while the walk happens.
3. The writers (`com.vaadin.hilla.generator.typescript`) fill in the
   TypeScript files from that model.

Every entry point — the Maven goal, the Gradle task, the Flow build task in
`engine-runtime` and the development server — runs the generation through
`TypeScriptProcessor.generate`. Do not add a second path that writes generated
files. The generator [README](../packages/java/typescript-generator/README.md)
describes the model, the writers and the tests in detail.

## Nullability

Whether a value can be `null` decides whether its generated TypeScript type is
`T` or `T | undefined`, so nullability is part of the contract.

- At build time, the `nonnull` plugin decides it from the annotations listed in
  `NonnullPluginConfig` (JSpecify, Jakarta, Spring and Hilla's own) and from
  Kotlin's type system for Kotlin classes. An annotation that is not on that
  list has no effect on the generated types — check it before assuming one
  does.
- At run time, `ExplicitNullableTypeChecker` checks that a value an endpoint
  returns is not `null` where the generated type says it can not be.
- `com.vaadin.hilla.Nonnull` is deprecated for removal; new code uses the
  JSpecify annotations.

A Java `null` is `undefined` in TypeScript, not `null`. For endpoint calls the
client maps between them when it handles the JSON (`Connect.ts`): `null` in a
response becomes `undefined`, and `undefined` in a request is sent as `null`.
`undefined` is what JavaScript uses for a variable or property that was never
set, so the mapping matches its meaning and lets client code simply leave out a
nullable value. Generated types and client code therefore use `undefined` and
`?`, never `null`. Values pushed over a subscription (`FluxConnection`) are not
mapped yet and can still arrive as `null`.

## Jackson

Hilla uses **Jackson 3** (`tools.jackson.*`): the endpoints serialize with it,
and the parser asks it what a class serializes as, so the generated types
follow the same rules as the serialized values. The annotations
(`com.fasterxml.jackson.annotation.*`) are the ones both Jackson versions
share. An application may still have a Jackson 2 object mapper of its own;
Hilla does not use it.

## Push: Flux and subscriptions

An endpoint method returning a `Flux` or an `EndpointSubscription` sends a
series of values instead of returning one. These go over a WebSocket connection
(`PushEndpoint`, `PushMessageHandler` on the server, `FluxConnection` in
`@vaadin/hilla-frontend`), not through `EndpointController`.

## Full-stack signals

Signals shared between the server and the browser are handled by
`com.vaadin.hilla.signals` (`SignalsHandler`, `SignalsRegistry`) on the server
and `@vaadin/hilla-react-signals` on the client. The generator maps the signal
types an endpoint returns to their client-side counterparts through the stand-in
types in `runtime-plugin-transfertypes`.

## Routing

Client-side views are routed by `@vaadin/hilla-file-router`, whose Vite plugin
turns the files under the views folder into routes. On the server, the
`route` package unifies those routes with server-side Flow routes, and applies
the access control of the client views.

## Hotswap in development mode

When classes change while the application runs, `Hotswapper` (a
`VaadinHotswapper`) decides whether the change affects the endpoints — a class
used in an endpoint changed, or an endpoint annotation was added — and asks
`EndpointCodeGenerator` to regenerate. Getting the regenerated files into the
browser is Vite's job.

The rule everything follows: hotswap re-runs the real generator, and never
derives what a generated file contains on its own. A second implementation
drifts from the build-time output, and the two then disagree about what the
application looks like.
