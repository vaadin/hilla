# Specification: Hilla Feature Toggles via application.properties

## Project Context

Hilla is a web framework that integrates a Spring Boot Java backend with a reactive TypeScript frontend. It provides type-safe server communication through automatic code generation from Java endpoints to TypeScript clients.

### Key Architecture

- **Monorepo**: Nx-based with Java packages (`packages/java/*`) and TypeScript packages (`packages/ts/*`)
- **Code Generation Pipeline**: Java -> OpenAPI 3.0 JSON -> TypeScript (via Node.js CLI)
- **Build-time flow**: Maven/Gradle plugin runs `ParserProcessor` (Java -> OpenAPI) then `GeneratorProcessor` (OpenAPI -> TypeScript via Node.js)
- **Runtime flow**: Spring Boot auto-configuration registers endpoint beans, route unification beans, push/signals infrastructure
- **Generator plugins** (TS side, executed sequentially): TransferTypes -> Backbone -> Client -> Barrel -> Model -> Push -> SubTypes -> Signals

### Features Targeted for Toggling

1. **File Router**: File-based routing (like Next.js). Powered by:
   - Spring beans: `RouteUnifyingServiceInitListener`, `RouteUtilConfiguration`, `RouteUnifyingConfiguration`
   - Vite plugin: `@vaadin/hilla-file-router` (scans `frontend/views/`, generates `file-routes.ts`)
   - Auto-configured via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

2. **Auto CRUD**: Automatic CRUD operations via `AutoGrid`, `AutoForm`, `AutoCrud` React components backed by `CrudRepositoryService`/`ListRepositoryService` Java base classes. Uses:
   - `@EndpointExposed` annotation on base service classes
   - Generated TypeScript form models (from `@vaadin/hilla-generator-plugin-model`)
   - Filter types, Pageable interfaces

3. **Vaadin UI**: Vaadin component integration including form models. Uses:
   - `@vaadin/hilla-generator-plugin-model` (generates `*Model.ts` files for form binding)
   - `@vaadin/react-components` (npm dependency, user-imported)
   - Lumo theme integration

## Task

Create a configuration mechanism using Spring Boot's standard `application.properties`, `application.yml`, or `application.yaml` that allows switching off these three features. Properties should default to `true` (all features enabled) for backward compatibility.

### Properties

```properties
# application.properties format
hilla.file-router.enabled=true    # default: true
hilla.auto-crud.enabled=true      # default: true
hilla.vaadin-ui.enabled=true      # default: true
```

```yaml
# application.yml / application.yaml format
hilla:
  file-router:
    enabled: true    # default: true
  auto-crud:
    enabled: true    # default: true
  vaadin-ui:
    enabled: true    # default: true
```

### File Precedence

Configuration files are checked in this order (first found wins):
1. `application.properties`
2. `application.yml`
3. `application.yaml`

### Constraints

- No files should be deleted, only modified with if-statement guards
- Default behavior (all true) must be identical to current behavior
- Build-time code generation must respect the toggles (read configuration from `src/main/resources/`)
- Runtime Spring beans must respect the toggles (via `@ConditionalOnProperty`)
- The Vite plugin must support an `enabled` option

### Existing Patterns

The codebase already has:
- `@ConditionalOnFeatureFlag("fullstackSignals")` on `SignalsConfiguration` (custom annotation using Vaadin's `FeatureFlags`)
- `@ConfigurationProperties("vaadin.endpoint")` on `EndpointProperties` for reading Spring properties
- `ConfigList` pattern for enabling/disabling generator plugins via Maven/Gradle XML config
