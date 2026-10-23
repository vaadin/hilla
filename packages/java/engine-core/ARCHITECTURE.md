# Engine and entry points

## Overview

The TypeScript of the endpoints is generated from four places: the Maven
plugin, the Gradle plugin, Flow's frontend build, and the development server
when classes change. All of them end in the same call,
`TypeScriptProcessor.generate`. This note explains how each one gets there,
what it passes along, and how the browser callable classes are found before
the parser walks them.

What goes in is an `EngineAutoConfiguration`: the project's folders, its
classpath, and optionally its main class. What comes out is the generated
TypeScript in the output folder. The stages are:

1. An entry point builds the configuration from what its build tool or the
   running application knows.
2. The browser callable classes are found, by a class scan, by Spring AOT, or
   from the beans of the running application.
3. `ParserProcessor` runs the parser over them, and `TypeScriptProcessor`
   writes the files.

The parser and the writers are described in
[the generator's architecture note](../typescript-generator/ARCHITECTURE.md).

## Design goals

- **One generation path.** Every entry point runs the generation through
  `TypeScriptProcessor.generate`, so the files written by a build, by Flow's
  task and by hotswap are always the same. A second path would drift from it.
- **Finding the endpoints without the build tool knowing about Spring.** The
  build plugins only describe the project. Which classes are browser callable
  is decided in `engine-core`, by a class scan or by asking Spring AOT in a
  separate JVM, so neither plugin depends on Spring or starts the application.
- **The build tools describe, the engine decides.** The Maven and Gradle code
  only fills in an `EngineAutoConfiguration`. Everything after that, including
  the choice of finder, is shared.

## Generating

[`TypeScriptProcessor`](src/main/java/com/vaadin/hilla/engine/TypeScriptProcessor.java)
has two `generate` methods:

- `generate(conf)` asks the configuration for its `BrowserCallableFinder`,
  finds the classes and continues with the other one. This is what the builds
  use.
- `generate(conf, browserCallables)` takes the classes as given. The
  development server uses it, since it already knows its beans.

Both parse with a new `ParserProcessor` and write with a new
`TypeScriptProcessor`, and return the `Generation` the files were written
from. Nothing is kept between runs except what the caller keeps.

`ParserProcessor` configures the parser with the classpath, the endpoint
annotations and the parser plugins, and always puts `EndpointModelPlugin`
first, so it is the last to leave a node, after the other plugins have said
everything about it.

## The configuration

[`EngineAutoConfiguration`](src/main/java/com/vaadin/hilla/engine/EngineAutoConfiguration.java)
holds what an entry point knows about the project: the base, build, classes
and output folders, the classpath, the group and artifact ids, the main class
and the "source classes", production mode, an optional Flow `ClassFinder`, and
the `ParserConfiguration`.

- It is built with `EngineAutoConfiguration.Builder`. A builder starts as a copy
  of another configuration, `getDefault()` when none is given. Relative paths
  are resolved against the base folder.
- `getDefault()` is a static instance. Without a build, it takes the working
  folder and `java.class.path`. The Maven goals and Gradle's `hillaConfigure`
  replace it with `setDefault`, so that Flow's task, which builds its
  configuration with `new Builder()`, inherits the project's classpath and
  main class from the build that runs it.
- `withDefaultAnnotations()` loads `@BrowserCallable`, `@Endpoint` and
  `@EndpointExposed` by name through the project's classpath. The engine does
  not depend on the endpoint module.

## Entry points

### Maven

[`Configurable.configure`](../maven-plugin/src/main/java/com/vaadin/hilla/maven/Configurable.java)
builds the configuration for every Hilla goal and sets it as the default. The
main class comes from the goal's `mainClass` parameter, or else from the
configuration of `spring-boot-maven-plugin`.

- `generate` (`EngineGenerateMojo`) calls `TypeScriptProcessor.generate`
  directly.
- `prepare-frontend` and `build-frontend` extend Flow's mojos. They only set
  the default configuration and then let Flow run its tasks, which reach the
  Flow task below.
- `configure` is deprecated and only prints a warning.

### Gradle

[`HillaPlugin`](../gradle-plugin/src/main/kotlin/com/vaadin/hilla/gradle/plugin/HillaPlugin.kt)
applies Flow's plugin and, in Spring Boot projects only, registers two tasks:

- `hillaConfigure` sets the default configuration. `vaadinBuildFrontend`
  depends on it, so Flow's build reaches the Flow task with the project's
  configuration, as in Maven.
- `hillaGenerate` calls `TypeScriptProcessor.generate` directly.

Both keep the configuration as `EngineConfigurationSettings`, a serializable
copy, because the Gradle configuration cache can't store an
`EngineAutoConfiguration`. The main class and the source classes come from the
`com.vaadin.hilla.mainClass` (or `mainClass`) and `com.vaadin.hilla.sourceClasses`
project properties.

### Flow's frontend build

Flow finds Hilla through its `EndpointGeneratorTaskFactory` service, which
[`EndpointGeneratorTaskFactoryImpl`](../engine-runtime/src/main/java/com/vaadin/hilla/internal/EndpointGeneratorTaskFactoryImpl.java)
in `engine-runtime` implements. Flow has two endpoint tasks: one to generate
the OpenAPI document and one to generate the endpoints from it.

- `createTaskGenerateOpenAPI` returns
  [`TaskGenerateTypeScriptImpl`](../engine-runtime/src/main/java/com/vaadin/hilla/internal/TaskGenerateTypeScriptImpl.java).
  Hilla no longer writes an OpenAPI document, but the task keeps Flow's name
  for historical reasons: it is the one that runs where the classes are
  available.
- `createTaskGenerateEndpoint` returns a task that does nothing.
- The task is skipped when Flow neither installs npm packages, builds a
  development bundle nor builds for production, which is the case for
  `prepare-frontend` and for a production server starting.
- The configuration is a copy of the default with Flow's folders, production
  mode and `ClassFinder`.

In production, the task calls `TypeScriptProcessor.generate(conf)`. In
development, it hands the work to the running application, below.

### The development server and hotswap

In development, the endpoints are the Spring beans of the running application.
[`EndpointCodeGenerator`](../endpoint/src/main/java/com/vaadin/hilla/EndpointCodeGenerator.java)
(a Spring component in the endpoint module) finds them with
`findBrowserCallables`, which asks the application context for the beans with
an endpoint annotation and unwraps proxies to their classes. It then calls
`TypeScriptProcessor.generate(conf, browserCallables)`.

- Flow's task runs through `ApplicationContextProvider.runOnContext`, which
  waits for the application context when it is not there yet. It uses the
  `EndpointCodeGenerator` bean if there is one, or else generates with the
  beans it finds itself.
- `EndpointCodeGenerator` remembers the classes the last generation was
  written from (`getClassesUsedInEndpoints`: the endpoints and the entities).
  When nothing has been generated yet, it runs the parser alone to learn them.
- [`Hotswapper`](../endpoint/src/main/java/com/vaadin/hilla/Hotswapper.java) is
  a `VaadinHotswapper`, registered as a service. It ignores changes to JDK,
  framework and library packages, and regenerates when a changed class is one
  of those classes, or carries an endpoint annotation.
- `EndpointCodeGenerator.update` finds the beans again and adds the changed
  classes that are annotated as browser callables, since a class that has just
  become one is not a bean yet. After generating, it calls
  `EndpointController.registerEndpoints`, so the server side matches.
- A failure is logged and not thrown: a class that can't be walked is usually
  one the developer is still writing.

The rule from [`architecture.md`](../../../guidelines/architecture.md#hotswap-in-development-mode)
holds here: hotswap re-runs the real generator and never works out a
generated file on its own.

## Finding the browser callable classes

A build has no running application, so it can't ask Spring for its beans. The
default `BrowserCallableFinder`, from
`EngineAutoConfiguration.getBrowserCallableFinder`, tries two finders in order
and uses the first one that does not fail.

1. [`LookupBrowserCallableFinder`](src/main/java/com/vaadin/hilla/engine/LookupBrowserCallableFinder.java)
   asks Flow's `ClassFinder` for the classes with an endpoint annotation, and
   fails on two endpoints with the same name. It needs a `ClassFinder`, so it
   only works in Flow's task. An empty result counts as success.
2. [`AotBrowserCallableFinder`](src/main/java/com/vaadin/hilla/engine/AotBrowserCallableFinder.java)
   runs Spring's `SpringApplicationAotProcessor` on the application in a
   separate JVM, with the project's classpath. The processor writes
   `reachability-metadata.json` under `spring-aot/main` in the build folder,
   which lists every bean class. The finder loads those classes and keeps the
   ones with an endpoint annotation. The arguments and the output of the
   process are kept in the build folder (`hilla-aot-args.txt`,
   `hilla-aot-report.txt`).

The AOT finder needs the application's main class. It takes the configured one,
or looks in the classes folders for a single class annotated with
`@SpringBootApplication`. When there is none but source classes are
configured, it runs Spring's own `SpringApplication` with those classes as
`spring.main.sources`.

When a main class or source classes are configured, only the AOT finder runs:
the configuration says the application is a Spring application whose beans
decide what is an endpoint, and a class scan could find annotated classes that
are not beans.

When every finder fails, the generation fails with a `ParserException` that
carries their exceptions as suppressed ones.

### Replacing the finder: `EngineConfiguration`

An application or a library can change how the engine finds classes by
providing one implementation of
[`EngineConfiguration`](src/main/java/com/vaadin/hilla/engine/EngineConfiguration.java)
through `ServiceLoader`. It receives the default finder and class loader and
returns the ones to use. More than one implementation is an error.

## The classpath each build tool provides

The parser and the AOT finder both load the application's classes, so the
classpath decides what they can see.

- **Maven**: the project's runtime classpath elements.
- **Gradle**: the runtime classpath of the configured source set, plus the
  Spring Boot loader tools from the build script's classpath.
- **Flow's task**: the classpath copied from the default, so the build's when
  a build set it. Classes are found through Flow's `ClassFinder`, and the
  class loader comes from it.
- **Development server**: the classpath of the running JVM. The classes come
  from the application context.

## Parser configuration

`ParserConfiguration` holds the endpoint annotations and the parser plugins.
`ParserConfiguration.PluginsProcessor` lists the default plugins:
`BackbonePlugin`, `MultipartFileCheckerPlugin`, `TransferTypesPlugin`, the
Kotlin nullability plugin when it and `kotlin-reflect` are on the classpath,
`NonnullPlugin`, `SubTypesPlugin` and `ModelPlugin`.

The plugin list is a `ConfigList`: the defaults, minus the ones in `disable`,
followed by the ones in `use`; with `disableAllDefaults`, only `use`. No entry
point sets it today: neither build plugin has a parameter for it, and
`ParserConfiguration.setPlugins` has no callers, so the generation always runs
with the defaults.

## Not used by the entry points

`com.vaadin.hilla.engine.commandrunner` (`MavenRunner`, `GradleRunner`) runs a
build tool as a process. Nothing outside its own tests uses it.
