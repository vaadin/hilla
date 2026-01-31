# Prompt for Next Session (Full Internet Access)

Copy the following prompt into a new Claude Code session with full internet access:

---

## Task: Verify and Test Hilla Feature Toggles Implementation

You are working on a Hilla fork repository.

### Context

Previous sessions implemented feature toggles for three Hilla features (file-router, auto-crud, vaadin-ui) driven by `application.properties`, `application.yml`, or `application.yaml`. All documentation is in `changes/01/`. Read these files first:

1. `changes/01/specification.md` - Full project context and task description
2. `changes/01/changenotes.md` - What was changed and why (10 files)
3. `changes/01/howtotest.md` - Detailed test instructions
4. `changes/01/futuresuggestions.md` - Future improvement ideas (11 remaining)

### What to Do

#### Phase 1: Build and Verify Compilation

1. Run `npm install` at repo root
2. Run `npm run build` to build all TypeScript packages
3. Run `mvn clean spotless:apply install -DskipTests` to build all Java packages
4. Fix any compilation errors in the changed files

#### Phase 2: Run Unit Tests

1. Run the Java unit test (22 tests covering .properties, .yml, .yaml, and precedence):
   ```bash
   mvn test -pl packages/java/engine-core -Dtest=HillaFeaturePropertiesTest
   ```
2. Run the TypeScript vite-plugin test:
   ```bash
   cd packages/ts/file-router && npx vitest run test/vite-plugin/vite-plugin.spec.ts
   ```
3. Run the full existing test suites to check for regressions:
   ```bash
   mvn test -pl packages/java/engine-core
   cd packages/ts/file-router && npx vitest run
   ```

#### Phase 3: Integration Verification

1. Check that `@ConditionalOnProperty` annotations compile correctly with Spring Boot 4.0.0-M3
2. Verify that `HillaFeatureProperties.fromBaseDir()` correctly reads `.properties`, `.yml`, and `.yaml` files with proper precedence
3. Verify that `GeneratorProcessor.preparePlugins()` correctly filters plugins when features are disabled
4. Verify the Vite plugin returns a proper no-op plugin when `enabled=false`

#### Phase 4: Additional Testing (if time permits)

1. Create a minimal Spring Boot test that verifies `@ConditionalOnProperty` works for the route beans:
   - Test that `RouteUnifyingServiceInitListener` is registered when property is missing (matchIfMissing=true)
   - Test that `RouteUnifyingServiceInitListener` is NOT registered when `hilla.file-router.enabled=false`
2. Test `GeneratorProcessor` with mocked `EngineAutoConfiguration` where features are disabled, verifying the correct plugins are filtered from the arguments list

#### Phase 5: Report

Generate a summary report with:
- Build status (Java + TypeScript)
- Test results (pass/fail counts)
- Any compilation errors found and fixes applied
- Any regressions detected
- Recommendations for additional changes needed

### Key Files to Review

| File | Purpose |
|---|---|
| `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/HillaFeatureProperties.java` | New: reads feature toggles from .properties/.yml/.yaml |
| `packages/java/engine-core/src/test/java/com/vaadin/hilla/engine/HillaFeaturePropertiesTest.java` | New: 22 unit tests |
| `packages/java/engine-core/pom.xml` | Modified: added SnakeYAML dependency |
| `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/EngineAutoConfiguration.java` | Modified: stores and propagates feature properties |
| `packages/java/engine-core/src/main/java/com/vaadin/hilla/engine/GeneratorProcessor.java` | Modified: filters plugins based on features |
| `packages/java/endpoint/src/main/java/com/vaadin/hilla/startup/RouteUnifyingServiceInitListener.java` | Modified: @ConditionalOnProperty added |
| `packages/java/endpoint/src/main/java/com/vaadin/hilla/route/RouteUtilConfiguration.java` | Modified: @ConditionalOnProperty added |
| `packages/java/endpoint/src/main/java/com/vaadin/hilla/route/RouteUnifyingConfiguration.java` | Modified: @ConditionalOnProperty added |
| `packages/ts/file-router/src/vite-plugin.ts` | Modified: enabled option added |
| `packages/ts/file-router/test/vite-plugin/vite-plugin.spec.ts` | Modified: test for disabled mode |

Commit and push any fixes with descriptive messages.

---
