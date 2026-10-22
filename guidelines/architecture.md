# Architecture

How the core pieces of Hilla fit together, and what to keep in mind when
touching them.

The more involved parts have their own architecture notes next to the code:

| Subsystem                       | Note                                                                                            |
| ------------------------------- | ----------------------------------------------------------------------------------------------- |
| Parser and TypeScript generator | [`typescript-generator/ARCHITECTURE.md`](../packages/java/typescript-generator/ARCHITECTURE.md) |
| Running the generation          | [`engine-core/ARCHITECTURE.md`](../packages/java/engine-core/ARCHITECTURE.md)                   |
| Form models                     | [`models/ARCHITECTURE.md`](../packages/ts/models/ARCHITECTURE.md)                               |
| Form binder                     | [`lit-form/ARCHITECTURE.md`](../packages/ts/lit-form/ARCHITECTURE.md)                           |
| Form binder in React            | [`react-form/ARCHITECTURE.md`](../packages/ts/react-form/ARCHITECTURE.md)                       |
| Endpoint calls and push         | [`frontend/ARCHITECTURE.md`](../packages/ts/frontend/ARCHITECTURE.md)                           |
| File router                     | [`file-router/ARCHITECTURE.md`](../packages/ts/file-router/ARCHITECTURE.md)                     |
| CRUD components and services    | [`react-crud/ARCHITECTURE.md`](../packages/ts/react-crud/ARCHITECTURE.md)                       |
| Internationalization runtime    | [`react-i18n/ARCHITECTURE.md`](../packages/ts/react-i18n/ARCHITECTURE.md)                       |

## What Vaadin Flow provides

Hilla applications don't use Flow's server-side UI, but many of the parts they
run on are provided by, or shared with, Flow and live in the Flow repository. A
change in one of these areas often needs a change in Flow, or is a Flow change
altogether.

- **Build plugins.** The Hilla Maven and Gradle plugins are entry points on top
  of Flow's: most Maven goals extend Flow's mojos, and the Gradle plugin
  applies Flow's plugin. The generation runs as a Flow build task
  (`engine-runtime`).
- **Frontend build.** The Vite build of a Hilla application is Flow's frontend
  build.
- **Application shell.** The `index.html` response, the application shell and
  static resource serving are Flow's; static resources are also served by
  Spring MVC. Hilla hooks into the `index.html` response with listeners such
  as `CsrfIndexHtmlRequestListener` and `RouteUnifyingIndexHtmlRequestListener`.
- **Offline support.** It relies on `@PWA`, the service worker in Flow's
  frontend build and the precache entries Flow provides.
- **Internationalization.** A frontend plugin in Flow collects the translation
  keys, and a Flow request handler serves the translation bundles;
  `@vaadin/hilla-react-i18n` is the runtime that loads them and changes the
  language.
- **Spring integration.** The `vaadin-spring` add-on is the Spring Boot entry
  point that brings in Hilla's endpoint controller, and integrates Spring
  Security for authorization, including stateless JWT authentication.
- **Routing.** Client-side views are combined with Flow's server-side routes
  and layouts: Flow's router serves server-side layouts around client views,
  and defines the `FileRouterRequestUtil` SPI that Hilla's `RouteUtil`
  implements for the access check of client views. The Hilla side of this —
  the `route` package and `@vaadin/hilla-file-router` — is described under
  [Routing](#routing) below.
- **Connection state.** The online/offline connection state
  (`window.Vaadin.connectionState`, which `ConnectClient` reports loading to)
  and the connection indicator component come from Flow.

## Calling an endpoint

A browser callable class is a Spring bean annotated with `@BrowserCallable`
(or the older `@Endpoint`). `@EndpointExposed` marks a superclass whose public
methods are exposed through the endpoints that extend it.

- `EndpointRegistry` discovers the browser callable beans at startup and
  registers their public methods. `EndpointNameChecker` rejects an endpoint
  name that is a reserved word in ECMAScript or TypeScript. A method or
  parameter with such a name is accepted. The writers declare it under a free
  name (`Names.free`), and export a method under its Java name again
  (`export { __delete as delete }`), so the application still calls
  `delete`; only a parameter's name changes in the generated TypeScript.
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

1. The parser walks the browser callable classes with reflection, and its
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
files. The [parser and generator note](../packages/java/typescript-generator/ARCHITECTURE.md)
describes the walk, the plugins, the model and the writers; the generator
[README](../packages/java/typescript-generator/README.md) describes the tests,
and the [engine note](../packages/java/engine-core/ARCHITECTURE.md) how each
entry point gets there.

## Nullability

Whether a value can be `null` decides whether its generated TypeScript type is
`T` or `T | undefined`, so nullability is part of the contract.

- At build time, the `nonnull` plugin decides it from the annotations listed in
  `NonnullPluginConfig` (JSpecify, Jakarta, Spring and Hilla's own) and from
  Kotlin's type system for Kotlin classes. A package annotated with JSpecify's
  `@NullMarked` (or Spring's `@NonNullApi`) makes its types non-null by
  default, in the generated TypeScript and in the run-time check below. An
  annotation that is not on that list has no effect on the generated types —
  check it before assuming one does.
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
`@vaadin/hilla-frontend`), not through `EndpointController`. See
[`frontend/ARCHITECTURE.md`](../packages/ts/frontend/ARCHITECTURE.md).

## Full-stack signals

Signals shared between the server and the browser are handled by
`com.vaadin.hilla.signals` (`SignalsHandler`, `SignalsRegistry`) on the server
and `@vaadin/hilla-react-signals` on the client. The generator maps the signal
types an endpoint returns to their client-side counterparts through the stand-in
types in `runtime-plugin-transfertypes`.

## Forms

The generator writes a model for every type an endpoint sends, built with
`@vaadin/hilla-models`: runtime reflection data about the Java type, its
properties and its constraints. The binder in `@vaadin/hilla-lit-form` holds a
form's value, validation and dirty state against such a model, and
`@vaadin/hilla-react-form` binds it to React; `@vaadin/hilla-react-crud` reads
the same models to build grids and forms. See the notes on
[models](../packages/ts/models/ARCHITECTURE.md), the
[binder](../packages/ts/lit-form/ARCHITECTURE.md) and its
[React hooks](../packages/ts/react-form/ARCHITECTURE.md).

## Routing

Client-side views are routed by `@vaadin/hilla-file-router`, whose Vite plugin
turns the files under the views folder into routes. On the server, the
`route` package unifies those routes with server-side Flow routes, and applies
the access control of the client views. See
[`file-router/ARCHITECTURE.md`](../packages/ts/file-router/ARCHITECTURE.md).

## Hotswap in development mode

When classes change while the application runs, `Hotswapper` (a
`VaadinHotswapper`) decides whether the change affects the endpoints — a class
used in an endpoint changed, or an endpoint annotation was added — and asks
`EndpointCodeGenerator` to regenerate (see the
[engine note](../packages/java/engine-core/ARCHITECTURE.md)). Getting the
regenerated files into the browser is Vite's job.

The rule everything follows: hotswap re-runs the real generator, and never
derives what a generated file contains on its own. A second implementation
drifts from the build-time output, and the two then disagree about what the
application looks like.
