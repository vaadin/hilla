# Hilla TypeScript Generator

This module consolidates the Java-to-TypeScript code generation functionality for Hilla. It walks the Java classes annotated with `@BrowserCallable` and writes the type-safe TypeScript the browser calls them through.

> [!IMPORTANT]
> The Java API of this module is **not public API**. It is used internally by
> Hilla: by the development mode server and the production build (the Maven
> plugin, the Gradle plugin and the Flow build tasks, through
> `TypeScriptProcessor.generate`), and by `hilla-endpoint` while it serves
> endpoint calls. Every class, method and package in it, including
> `com.vaadin.hilla.parser.*` and `com.vaadin.hilla.generator.*`, may change
> or be removed in any release, minor and maintenance ones included, without
> notice. Do not depend on it from application code or third-party libraries.
> Its entry points (`Parser`, `Plugin`, `AbstractPlugin`, `PluginConfiguration`
> and `TypeScriptWriter`) are marked `@Deprecated` so that such use gets a
> compiler warning, not because they are going away.
>
> The exceptions are
> `com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory`, which an
> application implements and registers as a bean with the
> `endpointMapperFactory` qualifier to customize how endpoint values are
> serialized, and `com.vaadin.hilla.parser.jackson.ByteArrayModule`, which a
> factory building its mapper from scratch registers to keep `byte[]` sent as
> an array of numbers. They remain public API.

For how the parser and the generator work, see [ARCHITECTURE.md](ARCHITECTURE.md).

## What's Inside

- **`com.vaadin.hilla.parser`** - The walk over the browser callable classes, and its plugins:
  - `backbone` - The endpoints, their methods and the types they send
  - `model` - What the validation and persistence annotations say about a value
  - `nonnull` - Nullability, for Java and Kotlin
  - `subtypes` - Polymorphic types
  - `transfertypes` - Types mapped to their stand-ins, and the `MultipartFile` check
- **`com.vaadin.hilla.generator`** - The model the writers need, and the writers of the TypeScript files

## Dependencies

- **Jackson** - Reads what a class serializes as, and the annotations of it
- **Kotlin** - Kotlin reflection and standard library for Kotlin support
- **Spring Data Commons** - For transfer type handling
- **Vaadin Flow** - Core Flow server APIs

## Tests

The generator is tested through its externally visible contract: a set of
browser callable Java classes must produce exactly the expected TypeScript
files, with the expected content, in the expected locations.

A test case is the outermost package holding browser callable classes. Those
classes, together with the ones of its subpackages, are generated and every
generated file is compared against the `snapshots` folder of the package in
the test resources. A generated file without a snapshot fails the test, and so
does a snapshot without a generated file. `EndpointGenerationTest` discovers
the cases and runs them, so **adding a case takes no test class at all**: add
a package with an endpoint in it and run the tests once with
`-Dhilla.test.updateSnapshots`, which creates the snapshots. A snapshots
folder left without endpoints to generate it fails the build.

Every case runs through the whole plugin chain, in the production order, so
what a test verifies is always what a real application would get.

The snapshots folder mirrors the output folder, with one exception: a
generated file is stored under the package of the type it belongs to, and the
folder already sits in the package of the test case, so that package is left
out of the path. A file of any other package, such as a mapped Spring Data
type, keeps its full path. Repeating the package made the deepest paths longer
than the 260 characters Windows allows a program which has not opted into long
paths, which broke the checkout of the repository;
`should_KeepSnapshotPathsWithinTheWindowsLimit` guards against that returning.

A case which needs more than the endpoint classes has its own test class
extending `AbstractFullStackTest` in its package. That package is skipped by
the discovery, and its classes are left out of the surrounding case, so that
every endpoint is covered exactly once. That is how the
configuration options of a plugin are covered:

```java
public class BasicTest extends AbstractFullStackTest {
    @Test
    public void should_ApplyNonNullAnnotation() {
        var plugin = new NonnullPlugin();
        plugin.setConfiguration(configuration);

        assertTypescriptMatchesSnapshot(
                generator(BasicEndpoint.class).withPlugin(plugin));
    }
}
```

A plugin can only be replaced by a configured instance of the same plugin: a
test cannot run a subset of the chain.

The generated client file is identical for every endpoint, so it is left out
of the comparison and verified once, by the test which asks for it with
`withClientFile()`.

Snapshots are recreated from the current generator output by running the tests
with `-Dhilla.test.updateSnapshots`. The resulting diff is the specification
of what the generator produces and must always be reviewed.

The tests need nothing but Java: the TypeScript is written here.

The TypeScript of the applications the integration tests run is compiled by
their own frontend build, which is where the generated files are type checked
against the packages they import from.

## Build

```bash
# Compile
mvn clean install

# Run tests
mvn test

# Format code
mvn spotless:apply
```
