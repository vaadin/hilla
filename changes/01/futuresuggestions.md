# Future Suggestions: Hilla Feature Toggles

## Implemented

- ~~Support `application.yml` / `application.yaml` format~~ — Done. `HillaFeatureProperties.fromBaseDir()` now checks `.properties`, `.yml`, and `.yaml` in precedence order using SnakeYAML.

## Immediate Enhancements

### 1. Support Spring Profiles

Properties like `application-dev.properties` or `application-prod.properties` are common. The build-time reader could accept an active profile parameter:

```java
public static HillaFeatureProperties fromBaseDir(Path baseDir, String activeProfile) {
    // Read application.properties first, then overlay application-{profile}.properties
}
```

The Maven/Gradle plugin already detects production mode, so this could be wired in.

### 2. More Granular Generator Plugin Toggles

Currently only the `model` plugin is toggled. Extend to other plugins:

```properties
hilla.generator.plugins.model.enabled=true
hilla.generator.plugins.signals.enabled=true
hilla.generator.plugins.push.enabled=true
hilla.generator.plugins.barrel.enabled=true
hilla.generator.plugins.subtypes.enabled=true
```

This gives fine-grained control over what TypeScript code gets generated.

### 3. Auto-CRUD Infrastructure Conditional Beans

The CRUD base classes (`CrudRepositoryService`, `ListRepositoryService`) are `@EndpointExposed` but not directly Spring-managed beans. Consider adding conditional registration for CRUD infrastructure:

- `JpaFilterConverter` bean conditional on `hilla.auto-crud.enabled`
- Filter-related OpenAPI schemas excluded when CRUD is disabled
- Parser-level filtering to exclude CRUD service interfaces from OpenAPI generation

### 4. Vite Plugin Reads from application.properties

Instead of requiring users to pass `enabled: false` in `vite.config.ts`, the Vite plugin could read `application.properties` directly:

```typescript
// In vite-plugin.ts
import { readFileSync } from 'fs';

function readHillaProperty(key: string, defaultValue: boolean): boolean {
  try {
    const props = readFileSync('src/main/resources/application.properties', 'utf-8');
    const match = props.match(new RegExp(`^${key}=(.+)$`, 'm'));
    return match ? match[1].trim() !== 'false' : defaultValue;
  } catch { return defaultValue; }
}
```

This would make the single `application.properties` truly the only configuration point.

## Medium-Term Improvements

### 5. Validation and Startup Warnings

When a feature is disabled but the user's code still references it:
- If `hilla.auto-crud.enabled=false` but `@BrowserCallable` services extend `CrudRepositoryService`, log a warning
- If `hilla.file-router.enabled=false` but `frontend/views/` directory exists with route files, log a warning

### 6. IDE Integration

Spring Boot configuration metadata (`META-INF/additional-spring-configuration-metadata.json`) for autocomplete in IntelliJ/VS Code:

```json
{
  "properties": [
    {
      "name": "hilla.file-router.enabled",
      "type": "java.lang.Boolean",
      "defaultValue": true,
      "description": "Enable/disable Hilla file-based routing"
    },
    {
      "name": "hilla.auto-crud.enabled",
      "type": "java.lang.Boolean",
      "defaultValue": true,
      "description": "Enable/disable Hilla auto-CRUD model generation"
    },
    {
      "name": "hilla.vaadin-ui.enabled",
      "type": "java.lang.Boolean",
      "defaultValue": true,
      "description": "Enable/disable Vaadin UI form model generation"
    }
  ]
}
```

### 7. Runtime Configuration Properties Bean

Create a proper `@ConfigurationProperties` class for runtime access:

```java
@ConfigurationProperties(prefix = "hilla")
public class HillaProperties {
    private FileRouter fileRouter = new FileRouter();
    private AutoCrud autoCrud = new AutoCrud();
    private VaadinUi vaadinUi = new VaadinUi();

    public static class FileRouter {
        private boolean enabled = true;
        // getters/setters
    }
    // ...
}
```

This would integrate with Spring Boot's property binding, validation, and relaxed binding (e.g., `HILLA_FILE_ROUTER_ENABLED` env var).

### 8. Conditional npm Dependency Installation

When features are disabled, their npm dependencies could be excluded from `package.json` generation or skipped during install:
- `hilla.vaadin-ui.enabled=false` -> skip `@vaadin/react-components` installation
- `hilla.auto-crud.enabled=false` -> skip `@vaadin/hilla-react-crud`

This would reduce bundle size and install time.

## Long-Term Vision

### 9. Feature Presets

Pre-defined configuration profiles:

```properties
# Minimal Hilla - only endpoints, no UI framework
hilla.preset=minimal

# Full Hilla - everything enabled (default)
hilla.preset=full

# API-only - endpoints + models, no routing/CRUD
hilla.preset=api
```

### 10. Build-Time Feature Detection

Automatically detect which features are actually used (by scanning imports, annotations) and skip unused ones without explicit configuration. This would be a zero-config optimization.

### 11. Gradle Plugin Support

The current implementation modifies Maven plugin path (`Configurable.java`) indirectly via `EngineAutoConfiguration`. The Gradle plugin (`HillaPlugin.kt`) should also be verified to propagate feature properties correctly through `EngineConfigurationSettings`.
