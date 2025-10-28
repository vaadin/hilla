# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Hilla is a web framework that integrates a Spring Boot Java backend with a reactive TypeScript frontend, providing type-safe server communication through automatic code generation from Java endpoints to TypeScript clients.

**Key Features:**
- Type-safe endpoints using `@BrowserCallable` annotation
- Automatic TypeScript generation from Java backend
- Support for both React and Lit frontends
- Plugin-based code generation architecture

## Build Commands

### Initial Setup
```bash
npm install
npm run build
mvn clean formatter:format install -DskipTests
```

### TypeScript Development
```bash
# Build all TypeScript packages
npm run build

# Build with alignment (syncs versions)
npm run build:align

# Clean build artifacts
npm run clean:build

# Lint
npm run lint
npm run lint:fix

# Type checking
npm run typecheck

# Run all tests
npm test

# Run tests with coverage
npm test:coverage

# Run React-specific tests
npm test:react
```

### Java Development
```bash
# Build and install (skip tests)
mvn clean install -DskipTests

# Format code according to Vaadin conventions
mvn formatter:format

# Run Java tests
mvn test

# Run integration tests (production build)
mvn verify -Pproduction

# Run single test class
mvn test -Dtest=ClassName

# Run specific test method
mvn test -Dtest=ClassName#methodName
```

### Testing Local Changes

**Java modules** (`packages/java/*`):
1. Build with `mvn clean install`
2. In your test app's `pom.xml`, set version to `25.0-SNAPSHOT`
3. Add Vaadin pre-release repository (see README.md for configuration)

**TypeScript modules** (`packages/ts/*`):
1. Navigate to modified package
2. Run `npm run build && npm pack`
3. In your test app: `npm install <path-to-tgz>`
4. Remove the package line from `package.json` when done

## Architecture

### Monorepo Structure

This is an Nx-based monorepo with:
- **Java packages**: `packages/java/*` (Maven modules)
- **TypeScript packages**: `packages/ts/*` (npm workspaces)
- **Test apps**: `packages/java/tests/*`

### Java Backend Architecture

#### Endpoint Layer (`packages/java/endpoint`)
- **Core annotations**: `@BrowserCallable`, `@AnonymousAllowed`, `@PermitAll`, `@RolesAllowed`
- **EndpointController**: Main HTTP endpoint handling (`/connect/{endpoint}/{method}`)
- **EndpointInvoker**: Invokes Java methods with type conversion
- **EndpointRegistry**: Discovers and registers `@BrowserCallable` endpoints
- Authentication and authorization handling

#### Engine (`packages/java/engine-core`, `packages/java/engine-runtime`)
- **EngineConfiguration**: Main configuration for code generation
- **CodeGenerationEngine**: Orchestrates OpenAPI generation and TypeScript generation
- Integrates with Maven/Gradle plugins

#### Parser (`packages/java/parser-jvm-*`)
- **parser-jvm-core**: Core Java parsing infrastructure using JavaParser
- **parser-jvm-plugin-***: Specialized plugins for:
  - `backbone`: Base OpenAPI structure
  - `model`: Java model to OpenAPI schema conversion
  - `nonnull`: Nullability analysis
  - `subtypes`: Polymorphic type handling
  - `transfertypes`: Data transfer object processing

Generates OpenAPI 3 specification from Java classes.

#### Maven/Gradle Plugins
- **maven-plugin** (`packages/java/maven-plugin`): Configures and runs code generation during Maven build
- **gradle-plugin** (`packages/java/gradle-plugin`): Same for Gradle builds

### TypeScript Frontend Architecture

#### Generator Core (`packages/ts/generator-core`)
- **Generator**: Main orchestrator that processes OpenAPI and runs plugins
- **PluginManager**: Manages plugin lifecycle and execution order
- **ReferenceResolver**: Resolves OpenAPI `$ref` references
- Plugin-based architecture - all code generation happens via plugins

#### Generator Plugins (`packages/ts/generator-plugin-*`)
Each plugin extends the base Plugin class and generates specific code:
- **backbone**: Base file structure and utilities
- **model**: TypeScript interfaces from OpenAPI schemas
- **client**: Endpoint client methods
- **barrel**: Index files for clean imports
- **push**: Server-push/Flux support
- **signals**: React Signals integration
- **subtypes**: Polymorphic type guards

#### Frontend Utilities
- **frontend** (`packages/ts/frontend`): Core utilities (Authentication, Connect client, Cookie management)
- **react-*** (`packages/ts/react-*`): React-specific hooks and components
  - `react-auth`: Authentication hooks
  - `react-form`: Form handling with type safety
  - `react-crud`: CRUD operations
  - `react-i18n`: Internationalization
  - `react-signals`: Reactive state management
- **lit-form** (`packages/ts/lit-form`): Form handling for Lit
- **file-router** (`packages/ts/file-router`): File-based routing

### Code Generation Flow

1. **Java → OpenAPI**: Parser plugins analyze Java endpoints and generate OpenAPI 3 spec
2. **OpenAPI → TypeScript**: Generator plugins read OpenAPI and generate TypeScript code
3. **Output**: Type-safe TypeScript clients matching Java endpoints exactly

Generated code location in apps: `frontend/generated/`

### Important Technical Details

#### Jackson 2/3 Hybrid Approach
Hilla uses **Jackson 3** (`tools.jackson.*`) for internal serialization but maintains **Jackson 2** (`com.fasterxml.jackson.*`) for OpenAPI/Swagger compatibility. See `JACKSON.md` for detailed migration notes.

Key points:
- Use `tools.jackson.databind.ObjectMapper` for Hilla serialization
- OpenAPI models still use Jackson 2 annotations
- `JsonPrinter` detects and routes to correct Jackson version

#### Testing Structure
- **packages/java/tests/spring/**: Spring Boot integration tests
- **packages/java/tests/gradle/**: Gradle plugin tests
- Each TypeScript package has its own test suite using Vitest

## Code Style

### TypeScript
- ESLint config: `.eslintrc.json` (extends Vaadin config)
- Prettier: Single quotes, 120 char width, trailing commas
- Use TypeScript 5.9+

### Java
- Formatter: Vaadin Java Conventions (`.config/VaadinJavaConventions.xml`)
- Java 21+ (source level)
- SLF4J for logging (java.util.logging is banned)

## Common Workflows

### Adding a New Generator Plugin
1. Create new package in `packages/ts/generator-plugin-{name}/`
2. Extend `Plugin` class from `@vaadin/hilla-generator-core`
3. Implement `execute(storage: SharedStorage): Promise<void>`
4. Register in generator configuration

### Adding a New Java Endpoint Feature
1. Modify/extend classes in `packages/java/endpoint`
2. Update parser plugins if OpenAPI generation needs changes
3. Add corresponding generator plugin if TypeScript generation needs changes
4. Add integration tests in `packages/java/tests/spring/`

### Debugging Code Generation
1. Check OpenAPI output in application's `target/generated-resources/openapi.json`
2. Check generated TypeScript in `frontend/generated/`
3. Enable debug logging in EngineConfiguration

## Dependencies

### Required Versions
- **Node.js**: >= 22 LTS
- **npm**: >= 10 (lockfile v3)
- **JDK**: >= 17 (source level 21)
- **Maven**: >= 3

### Key Java Dependencies
- Spring Boot 4.0.0-M3
- Jackson 3.0.0-rc9 (with Jackson 2 for OpenAPI)
- Vaadin Flow 25.0-SNAPSHOT
- Swagger Parser 2.1.15

### Key TypeScript Dependencies
- TypeScript 5.9.3
- Vite 7.1.9
- Vitest 3.1.1
- React 19.2.0
- Lit 3.3.1

## CI/CD
- GitHub Actions workflow: `.github/workflows/validation.yml`
- Runs on push to main and PRs
- Parallel test execution for Java tests
- Coverage reporting with JaCoCo (Java) and c8 (TypeScript)
