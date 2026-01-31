# Change Notes: Hilla Feature Toggles

## Summary

Added `application.properties`-driven feature toggles for file-router, auto-crud, and vaadin-ui. All default to `true` for backward compatibility.

## Files Changed

### New Files

#### 1. `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/HillaFeatureProperties.java`

POJO that reads three boolean properties from `application.properties`:
- `hilla.file-router.enabled` (default: true)
- `hilla.auto-crud.enabled` (default: true)
- `hilla.vaadin-ui.enabled` (default: true)

Key methods:
- `defaults()` - returns instance with all features enabled
- `fromBaseDir(Path baseDir)` - reads `src/main/resources/application.properties` from the project base directory. Falls back to all-true defaults if file is missing or unreadable.
- `isFileRouterEnabled()`, `isAutoCrudEnabled()`, `isVaadinUiEnabled()` - getters

This works at **build time** (Maven/Gradle) by reading the properties file directly from the filesystem, without needing a Spring context.

#### 2. `packages/java/engine-core/src/test/java/com/vaadin/hilla/engine/HillaFeaturePropertiesTest.java`

9 unit tests covering:
- All defaults enabled
- Missing properties file -> defaults
- Empty properties file -> defaults
- Each feature disabled individually
- All features disabled together
- Explicit true values
- Mixed properties with unrelated keys
- Missing individual properties default to true
- Builder retains feature properties on `EngineAutoConfiguration`

### Modified Files

#### 3. `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/EngineAutoConfiguration.java`

Changes:
- Added `HillaFeatureProperties featureProperties` field
- Added `getFeatureProperties()` getter (lazy-loads via `HillaFeatureProperties.fromBaseDir(baseDir)` if not explicitly set)
- Added `Builder.featureProperties(HillaFeatureProperties)` method
- Builder constructor copies `featureProperties` from source configuration

#### 4. `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/GeneratorProcessor.java`

Changes:
- Added `HillaFeatureProperties featureProperties` field, initialized from `conf.getFeatureProperties()` in constructor
- Added `import java.util.Set`
- Modified `preparePlugins(List<Object> arguments)` to filter disabled plugins via `getDisabledPlugins()`
- Added `getDisabledPlugins()` method that returns a `Set<String>` of plugin paths to skip:
  - When `vaadinUiEnabled=false` -> skips `@vaadin/hilla-generator-plugin-model`
  - When `autoCrudEnabled=false` -> skips `@vaadin/hilla-generator-plugin-model`
- Logs info messages when plugins are skipped

#### 5. `packages/java/endpoint/src/main/java/com/vaadin/hilla/startup/RouteUnifyingServiceInitListener.java`

Changes:
- Added import: `org.springframework.boot.autoconfigure.condition.ConditionalOnProperty`
- Added annotation: `@ConditionalOnProperty(name = "hilla.file-router.enabled", matchIfMissing = true)`

When `hilla.file-router.enabled=false`, this Spring bean is not registered. This disables the route unification listener that injects server-side routes into HTML and handles dynamic route info requests.

#### 6. `packages/java/endpoint/src/main/java/com/vaadin/hilla/route/RouteUtilConfiguration.java`

Changes:
- Added import: `org.springframework.boot.autoconfigure.condition.ConditionalOnProperty`
- Added annotation: `@ConditionalOnProperty(name = "hilla.file-router.enabled", matchIfMissing = true)`

When disabled, the `RouteUtil` bean (which implements `FileRouterRequestUtil`) is not registered.

#### 7. `packages/java/endpoint/src/main/java/com/vaadin/hilla/route/RouteUnifyingConfiguration.java`

Changes:
- Added import: `org.springframework.boot.autoconfigure.condition.ConditionalOnProperty`
- Added annotation: `@ConditionalOnProperty(name = "hilla.file-router.enabled", matchIfMissing = true)`

When disabled, the `RouteUnifyingConfigurationProperties` bean is not registered.

#### 8. `packages/ts/file-router/src/vite-plugin.ts`

Changes:
- Added `enabled?: boolean` to `PluginOptions` type (default: `true`)
- Added early return in `vitePluginFileSystemRouter()`: when `enabled=false`, returns a no-op plugin object with only the `name` property (no `configResolved`, `buildStart`, `hotUpdate`, or `transform` hooks)

#### 9. `packages/ts/file-router/test/vite-plugin/vite-plugin.spec.ts`

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
     -> HillaFeatureProperties.fromBaseDir(baseDir) reads application.properties
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
