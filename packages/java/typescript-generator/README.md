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

## Build

```bash
# Compile
mvn clean install

# Run tests
mvn test

# Format code
mvn formatter:format
```

## Migration Notes

This module was created by consolidating 9 separate `parser-jvm-*` modules to:
- Simplify the build structure
- Reduce inter-module dependencies
- Improve maintainability
- Speed up builds

All package names remain unchanged (`com.vaadin.hilla.parser.*`), so existing code using these classes should continue to work with only a dependency update.
