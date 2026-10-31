# Repository

## Technology stack

- **Java 21+** (source level), **Maven** multi-module build; the Gradle plugin
  is built with Gradle.
- **Kotlin** in the parser's Kotlin support (the Kotlin nullability plugin
  and the Kotlin nodes of the `backbone` plugin, plus their tests), in the
  Gradle plugin, and in the `tests/gradle/kotlin-gradle-test` application and
  its integration tests.
- **Spring Boot 4** — endpoints are Spring beans served by Spring MVC.
- **Vaadin Flow** — Hilla applications don't use Flow's server-side UI, but
  Flow provides much of what they run on: the build plugins and the Vite
  frontend build, the application shell, PWA and offline support, the i18n
  plumbing, the Spring integration (`vaadin-spring`) and the connection
  state. See [Architecture](architecture.md#what-vaadin-flow-provides).
- **Jackson 3** (`tools.jackson.*`) for endpoint serialization and for reading
  what a class serializes as.
- **TypeScript**, **React** and **Lit** for the frontend packages, built and
  tested with **Vite** and **Vitest**, orchestrated by **Nx** over npm
  workspaces.
- **JUnit** and **Mockito** for Java unit tests, **Vaadin TestBench** for
  browser integration tests.

## Package structure

This is a monorepo. Java modules live in `packages/java/`, TypeScript packages
in `packages/ts/`.

| Java module                    | Contents                                                                                   |
| ------------------------------ | ------------------------------------------------------------------------------------------ |
| `endpoint`                     | Runtime: `@BrowserCallable`, `EndpointController`, `EndpointInvoker`, `EndpointRegistry`, security, push, signals, hotswap. |
| `engine-core`                  | `EngineConfiguration`, `ParserProcessor`, `TypeScriptProcessor`: runs the generation for a build. |
| `engine-runtime`               | The Flow build task that runs the generation (`TaskGenerateTypeScriptImpl`).               |
| `typescript-generator`         | The parser and its plugins, the generator model and the TypeScript writers.               |
| `runtime-plugin-transfertypes` | Stand-in types the generator maps types such as Spring Data `Pageable`, `Flux` and signals to. |
| `maven-plugin`                 | Maven goals that run the generation.                                                      |
| `gradle-plugin`                | Gradle tasks that run the generation (separate Gradle build).                             |
| `hilla`, `hilla-bom`           | Aggregate artifact and bill of materials.                                                 |
| `tests/`                       | Test applications and their integration tests (`tests/spring/*`, `tests/gradle/*`).       |

| TypeScript package | Contents                                                                       |
| ------------------ | ------------------------------------------------------------------------------ |
| `frontend`         | `ConnectClient`, endpoint errors, authentication, CSRF, `FluxConnection`.      |
| `models`           | Models the generated TypeScript describes types through, used by the forms.    |
| `lit-form`         | Form binder for Lit.                                                           |
| `react-form`       | Form binder for React.                                                         |
| `react-auth`       | Authentication hooks for React.                                                |
| `react-crud`       | CRUD components (`AutoGrid`, `AutoForm`, `AutoCrud`).                          |
| `react-i18n`       | Internationalization.                                                          |
| `react-signals`    | Client side of full-stack signals.                                             |
| `file-router`      | File-based routing: Vite plugin and runtime.                                   |
| `generator-utils`  | Helpers for writing TypeScript, used by the file router.                       |

## The contract between the packages

The Java runtime, the generated TypeScript and the TypeScript packages form one
contract: the generated client calls `ConnectClient` from
`@vaadin/hilla-frontend`, the generated models are built with
`@vaadin/hilla-models`, and the wire format they use is what
`EndpointController` and `EndpointInvoker` produce and accept. A change on one side usually needs the matching change on
the other side in the same PR.

## Code style

- **Java:** formatted with Spotless using the Vaadin Java conventions
  (`.config/VaadinJavaConventions.xml`) and the import order in
  `.config/hilla.importorder`. Run `mvn spotless:apply` before every commit —
  CI runs `mvn spotless:check`. Log through SLF4J; `java.util.logging` is a
  banned import.
- **TypeScript:** ESLint (`.eslintrc.json`, Vaadin config) and Prettier
  (`.prettierrc`: single quotes, 120 columns, trailing commas). CI runs
  `npm run check` (lint and type check of every package).

## Blast radius

- Changes to the parser, its plugins or the generator model change the
  generated TypeScript of every application. The snapshot diff shows exactly
  how; review it as part of the change.
- Changes to `EndpointController`, `EndpointInvoker` or the serialization
  change the wire format that the generated clients of already built
  applications rely on.
- Changes to a TypeScript package's exports can break both application code
  and the generated TypeScript that imports from it.
