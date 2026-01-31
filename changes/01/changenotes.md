# Change Notes: Hilla Feature Toggles

## Summary

Added feature toggles for file-router, auto-crud, and vaadin-ui driven by `application.properties`, `application.yml`, or `application.yaml`. All default to `true` for backward compatibility. Files are checked in precedence order: `.properties` > `.yml` > `.yaml` (first found wins).

## Files Changed

### New Files

#### 1. `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/HillaFeatureProperties.java`

Reads three boolean properties from the application configuration file:
- `hilla.file-router.enabled` (default: true)
- `hilla.auto-crud.enabled` (default: true)
- `hilla.vaadin-ui.enabled` (default: true)

Supports `.properties` format (via `java.util.Properties`) and YAML format (via SnakeYAML). For YAML, navigates nested map structure (e.g. `hilla.file-router.enabled` maps to `hilla: { file-router: { enabled: false } }`). Handles both native YAML booleans and quoted string values.

Key methods:
- `defaults()` - returns instance with all features enabled
- `fromBaseDir(Path baseDir)` - checks for `application.properties`, then `application.yml`, then `application.yaml` under `src/main/resources/`. Uses the first file found. Falls back to all-true defaults if no file exists or cannot be read.
- `isFileRouterEnabled()`, `isAutoCrudEnabled()`, `isVaadinUiEnabled()` - getters

This works at **build time** (Maven/Gradle) by reading the configuration file directly from the filesystem, without needing a Spring context.

#### 2. `packages/java/engine-core/src/test/java/com/vaadin/hilla/engine/HillaFeaturePropertiesTest.java`

22 unit tests covering:
- All defaults enabled
- Missing configuration file -> defaults
- Empty `.properties` file -> defaults
- Each feature disabled individually (`.properties`)
- All features disabled together (`.properties`)
- Explicit true values (`.properties`)
- Mixed properties with unrelated keys
- Missing individual properties default to true
- `.yml` file: all disabled, single disabled, explicitly enabled, empty file, no hilla section, mixed content, quoted string values
- `.yaml` extension support
- Precedence: `.properties` over `.yml`, `.yml` over `.yaml`, `.yaml` when others absent
- Builder retains feature properties on `EngineAutoConfiguration`

### Modified Files

#### 3. `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/EngineAutoConfiguration.java`

Changes:
- Added `HillaFeatureProperties featureProperties` field
- Added `getFeatureProperties()` getter (lazy-loads via `HillaFeatureProperties.fromBaseDir(baseDir)` if not explicitly set)
- Added `Builder.featureProperties(HillaFeatureProperties)` method
- Builder constructor copies `featureProperties` from source configuration

#### 4. `packages/java/engine-core/pom.xml`

Changes:
- Added `org.yaml:snakeyaml` dependency (version managed by Spring Boot BOM via `spring-boot-dependencies`)

#### 5. `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/GeneratorProcessor.java`

Changes:
- Added `HillaFeatureProperties featureProperties` field, initialized from `conf.getFeatureProperties()` in constructor
- Added `import java.util.HashSet` and `import java.util.Set`
- Modified `preparePlugins(List<Object> arguments)` to filter disabled plugins via `getDisabledPlugins()`
- Added `getDisabledPlugins()` method that returns a `Set<String>` of plugin paths to skip:
  - When `vaadinUiEnabled=false` -> skips `@vaadin/hilla-generator-plugin-model`
  - When `autoCrudEnabled=false` -> skips `@vaadin/hilla-generator-plugin-model`
- Logs info messages when plugins are skipped

#### 6. `packages/java/endpoint/src/main/java/com/vaadin/hilla/startup/RouteUnifyingServiceInitListener.java`

Changes:
- Added import: `org.springframework.boot.autoconfigure.condition.ConditionalOnProperty`
- Added annotation: `@ConditionalOnProperty(name = "hilla.file-router.enabled", matchIfMissing = true)`

When `hilla.file-router.enabled=false`, this Spring bean is not registered. This disables the route unification listener that injects server-side routes into HTML and handles dynamic route info requests.

#### 7. `packages/java/endpoint/src/main/java/com/vaadin/hilla/route/RouteUtilConfiguration.java`

Changes:
- Added import: `org.springframework.boot.autoconfigure.condition.ConditionalOnProperty`
- Added annotation: `@ConditionalOnProperty(name = "hilla.file-router.enabled", matchIfMissing = true)`

When disabled, the `RouteUtil` bean (which implements `FileRouterRequestUtil`) is not registered.

#### 8. `packages/java/endpoint/src/main/java/com/vaadin/hilla/route/RouteUnifyingConfiguration.java`

Changes:
- Added import: `org.springframework.boot.autoconfigure.condition.ConditionalOnProperty`
- Added annotation: `@ConditionalOnProperty(name = "hilla.file-router.enabled", matchIfMissing = true)`

When disabled, the `RouteUnifyingConfigurationProperties` bean is not registered.

#### 9. `packages/ts/file-router/src/vite-plugin.ts`

Changes:
- Added `enabled?: boolean` to `PluginOptions` type (default: `true`)
- Added early return in `vitePluginFileSystemRouter()`: when `enabled=false`, returns a no-op plugin object with only the `name` property (no `configResolved`, `buildStart`, `hotUpdate`, or `transform` hooks)

#### 10. `packages/ts/file-router/test/vite-plugin/vite-plugin.spec.ts`

Changes:
- Added new describe block `'vite-plugin disabled'` with 1 test:
  - `'should return a no-op plugin when enabled is false'` - verifies the plugin has only the `name` property and no lifecycle hooks

## How the Toggle Flows Through the System

### Build-Time (Maven/Gradle)

```
Maven/Gradle plugin executes
  -> EngineAutoConfiguration.Builder.build()
  -> EngineAutoConfiguration stores baseDir
  -> GeneratorProcessor(conf) reads conf.getFeatureProperties()
     -> HillaFeatureProperties.fromBaseDir(baseDir) checks for:
        1. application.properties  (java.util.Properties)
        2. application.yml         (SnakeYAML)
        3. application.yaml        (SnakeYAML)
        First file found wins; if none, defaults to all-enabled
  -> preparePlugins() filters disabled plugins from Node.js CLI arguments
  -> Node.js runs generator-cli with only enabled plugins
```

### Runtime (Spring Boot)

```
Spring Boot starts
  -> Auto-configuration processes beans
  -> @ConditionalOnProperty checks application.properties
  -> If hilla.file-router.enabled=false:
     RouteUnifyingServiceInitListener NOT registered
     RouteUtilConfiguration NOT registered
     RouteUnifyingConfiguration NOT registered
```

### Vite Plugin (Frontend Build)

```
User's vite.config.ts
  -> vitePluginFileSystemRouter({ enabled: false })
  -> Returns no-op plugin, no route files generated
```
