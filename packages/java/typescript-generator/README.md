# Hilla TypeScript Generator

This module consolidates the Java-to-TypeScript code generation functionality for Hilla. It parses Java endpoints annotated with `@BrowserCallable` and generates OpenAPI specifications, which are then used to create type-safe TypeScript clients.

## What's Inside

This module consolidates the following previously separate modules:

- **parser-jvm-core** - Core parsing infrastructure using JavaParser and ClassGraph
- **parser-jvm-utils** - OpenAPI and Jackson utilities for serialization
- **parser-jvm-test-utils** - Shared testing utilities
- **parser-jvm-plugin-backbone** - Base OpenAPI structure generation
- **parser-jvm-plugin-model** - Java model to OpenAPI schema conversion
- **parser-jvm-plugin-nonnull** - Nullability analysis for Java
- **parser-jvm-plugin-nonnull-kotlin** - Kotlin nullability support
- **parser-jvm-plugin-subtypes** - Polymorphic type handling
- **parser-jvm-plugin-transfertypes** - Data transfer object processing

## Architecture

### Core Components

- **Plugin System**: Extensible plugin architecture for code generation
- **OpenAPI Generation**: Converts Java classes and methods to OpenAPI 3 specification
- **Jackson Hybrid**: Uses Jackson 3 for internal serialization, Jackson 2 for OpenAPI compatibility

### Package Structure

```
com.vaadin.hilla.parser
├── core/              - Core parsing infrastructure
├── models/            - OpenAPI model classes
└── plugins/           - Plugin implementations
    ├── backbone/      - Base structure generation
    ├── model/         - Schema generation
    ├── nonnull/       - Nullability analysis
    │   └── kotlin/    - Kotlin-specific nullability (Kotlin source)
    ├── subtypes/      - Polymorphic types
    └── transfertypes/ - DTO processing
```

## Dependencies

### External Dependencies
- **Swagger Core** - OpenAPI 3 model support (Jackson 2)
- **ClassGraph** - Fast classpath scanning
- **Jackson 2 & 3** - Hybrid JSON serialization approach
- **Kotlin** - Kotlin reflection and standard library for Kotlin support
- **Spring Data Commons** - For transfer type handling
- **Vaadin Flow** - Core Flow server APIs

### Downstream Consumers
- `hilla-engine-core` - Orchestrates code generation
- `hilla-engine-runtime` - Runtime code generation
- `hilla-maven-plugin` - Maven build integration
- `hilla-endpoint` - Endpoint processing

## Tests

The generator is tested through its externally visible contract: a set of
browser callable Java classes must produce exactly the expected TypeScript
files, with the expected content, in the expected locations.

A test extends `AbstractFullStackTest` and names the classes to generate from:

```java
public class SimpleTypeTest extends AbstractFullStackTest {
    @Test
    public void should_UseAppropriateSchema_When_SimpleTypesAreUsed() {
        assertTypescriptMatchesSnapshot(SimpleTypeEndpoint.class);
    }
}
```

The complete pipeline is executed with the same parser and generator plugins as
a real application, and every generated file is compared against the TypeScript
files in the `snapshots` folder of the test resources of the test package. A
generated file without a snapshot fails the test, and so does a snapshot
without a generated file.

Use `generator(...)` instead when a test needs a specifically configured
plugin or an extended classpath:

```java
assertTypescriptMatchesSnapshot(
        generator(BasicEndpoint.class).withPlugin(configuredPlugin));
```

The generated client file is identical for every endpoint, so it is left out
of the comparison unless a test asks for it with `withClientFile()`.

The `snapshots` folder belongs to the package of the test, not to the test
class, so keep one full-stack test class per package: two of them in the same
package would share, and overwrite, the same expected files.

Snapshots are recreated from the current generator output by running the tests
with `-Dhilla.test.updateSnapshots`. The resulting diff is the specification
of what the generator produces and must always be reviewed.

Note that the tests run the TypeScript generator, so the npm packages need to
be built first (`npm ci && npm run build` in the repository root).

## Build

```bash
# Compile
mvn clean install

# Run tests
mvn test

# Format code
mvn spotless:apply
```

## Migration Notes

This module was created by consolidating 9 separate `parser-jvm-*` modules to:
- Simplify the build structure
- Reduce inter-module dependencies
- Improve maintainability
- Speed up builds

All package names remain unchanged (`com.vaadin.hilla.parser.*`), so existing code using these classes should continue to work with only a dependency update.
