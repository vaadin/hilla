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
mvn clean spotless:apply install -DskipTests
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
mvn spotless:apply

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
2. In your test app's `pom.xml`, set version to `25.3-SNAPSHOT`
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
- **ParserProcessor / TypeScriptProcessor**: Walk the browser callable classes and write the TypeScript of them
- Integrates with Maven/Gradle plugins

#### Parser and generator (`packages/java/typescript-generator`)
- **parser core**: Walks the browser callable classes with ClassGraph
- **plugins**: Say what the walk finds:
  - `backbone`: The endpoints, their methods and the types they send
  - `model`: What the validation and persistence annotations say about a value
  - `nonnull`: Nullability analysis
  - `subtypes`: Polymorphic type handling
  - `transfertypes`: Data transfer object processing
- **generator**: The model the writers need, and the TypeScript files written
  from it

#### Maven/Gradle Plugins
- **maven-plugin** (`packages/java/maven-plugin`): Configures and runs code generation during Maven build
- **gradle-plugin** (`packages/java/gradle-plugin`): Same for Gradle builds

### TypeScript Frontend Architecture

#### Generating the endpoint TypeScript
The TypeScript of the endpoints is written in Java, by
`packages/java/typescript-generator`: see its README. The generator which
used to do it in Node is gone, along with its packages; `generator-utils`
stays, since the file router writes its routes with it.

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

1. **The walk**: The parser plugins walk the browser callable classes and say what
   they find about the endpoints, their methods and the types of the values
2. **The model**: A plugin collects what the writers need while that happens
3. **The writers**: The TypeScript files are filled in from the model
4. **Output**: Type-safe TypeScript clients matching Java endpoints exactly

Generated code location in apps: `frontend/generated/`

### Important Technical Details

#### Jackson
Hilla uses **Jackson 3** (`tools.jackson.*`): the endpoints serialize with it,
and the parser asks it what a class serializes as. The annotations
(`com.fasterxml.jackson.annotation.*`) are the ones both versions share.

Key points:
- Use `tools.jackson.databind.ObjectMapper` for Hilla serialization
- An application may still have an object mapper of Jackson 2 of its own

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

### Writing Something New in the Generated TypeScript
1. Add what the writers need to the model in
   `packages/java/typescript-generator/src/main/java/com/vaadin/hilla/generator/model`
2. Collect it in `EndpointModelPlugin` while the parser walks the classes
3. Write it in the matching writer under `.../generator/typescript`
4. Add a test case, and run the tests once with `-Dhilla.test.updateSnapshots`

### Adding a New Java Endpoint Feature
1. Modify/extend classes in `packages/java/endpoint`
2. Update parser plugins if what the parser has to see changes
3. Extend the Java TypeScript writers if the generated TypeScript changes
4. Add integration tests in `packages/java/tests/spring/`

### Debugging Code Generation
1. Check generated TypeScript in `frontend/generated/`, which
   `generated-file-list.txt` lists
2. Enable debug logging in EngineConfiguration

## Dependencies

### Required Versions
- **Node.js**: >= 22 LTS
- **npm**: >= 10 (lockfile v3)
- **JDK**: >= 17 (source level 21)
- **Maven**: >= 3

### Key Java Dependencies
- Spring Boot 4.1.1
- Jackson 3.1.5
- Vaadin Flow 25.4-SNAPSHOT

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
