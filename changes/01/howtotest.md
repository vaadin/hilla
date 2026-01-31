# How to Test: Hilla Feature Toggles

## Prerequisites

- JDK 17+
- Node.js 22+
- Maven 3+
- `npm install` at repo root
- `npm run build` for TypeScript packages

## 1. Unit Tests

### Java: HillaFeatureProperties

```bash
cd /home/user/haru
mvn test -pl packages/java/engine-core -Dtest=HillaFeaturePropertiesTest
```

This runs 22 tests covering:
- Default values (all true)
- Missing configuration file -> defaults
- Empty `.properties` file -> defaults
- Individual feature disable/enable (`.properties`)
- All features disabled (`.properties`)
- Mixed properties with unrelated keys
- `.yml` file: all disabled, single disabled, explicitly enabled, empty file, no hilla section, mixed content, quoted string values
- `.yaml` extension support
- Precedence: `.properties` > `.yml` > `.yaml`
- Builder integration with EngineAutoConfiguration

### TypeScript: Vite Plugin disabled mode

```bash
cd /home/user/haru
npm run build   # build TS packages first
cd packages/ts/file-router
npx vitest run test/vite-plugin/vite-plugin.spec.ts
```

Expected: 12 tests pass (11 existing + 1 new for `enabled=false`).

## 2. Integration Testing

### Test File Router Toggle

1. Create or use a Hilla test app (e.g., `packages/java/tests/spring/react-grid-test`)

2. **With feature enabled (default)**:
   ```properties
   # application.properties - no hilla.file-router.enabled set, or:
   hilla.file-router.enabled=true
   ```
   - Start the app: `mvn spring-boot:run`
   - Verify: file-based routes work, `RouteUnifyingServiceInitListener` is registered
   - Check logs: no "Skipping" messages

3. **With feature disabled (properties format)**:
   ```properties
   # application.properties
   hilla.file-router.enabled=false
   ```
   - Start the app: `mvn spring-boot:run`
   - Verify: `RouteUnifyingServiceInitListener` bean is NOT created
   - Check Spring debug logs: `@ConditionalOnProperty` should show the bean was skipped
   - Routes should NOT be unified (server-side routes not injected into client)

4. **With feature disabled (YAML format)**:
   ```yaml
   # application.yml
   hilla:
     file-router:
       enabled: false
   ```
   - Same verification as step 3

### Test Auto-CRUD Toggle

1. **With feature enabled (default)**:
   ```properties
   # application.properties - no hilla.auto-crud.enabled set
   ```
   - Run code generation: `mvn hilla:generate`
   - Verify: `frontend/generated/` contains `*Model.ts` files
   - Verify: AutoGrid/AutoForm components work with generated models

2. **With feature disabled**:
   ```properties
   # application.properties
   hilla.auto-crud.enabled=false
   ```
   - Run code generation: `mvn hilla:generate`
   - Check logs: should see `"Auto CRUD disabled: skipping model generator plugin"`
   - Verify: `frontend/generated/` does NOT contain `*Model.ts` files
   - Endpoint files and entity interfaces should still be generated

### Test Vaadin UI Toggle

1. **With feature disabled**:
   ```properties
   # application.properties
   hilla.vaadin-ui.enabled=false
   ```
   - Run code generation: `mvn hilla:generate`
   - Check logs: should see `"Vaadin UI disabled: skipping model generator plugin"`
   - Verify: No `*Model.ts` files generated
   - Endpoint files and basic TypeScript interfaces still generated

### Test Vite Plugin enabled Option

```typescript
// vite.config.ts
import fileRouter from '@vaadin/hilla-file-router';

export default {
  plugins: [
    fileRouter({ enabled: false }), // no-op
  ],
};
```

- Verify: No `file-routes.ts` or `file-routes.json` generated in `frontend/generated/`
- Verify: No errors during Vite build

## 3. Backward Compatibility Testing

Run all existing tests to verify nothing breaks when no properties are set:

```bash
# Java tests
mvn test -pl packages/java/engine-core

# TypeScript tests
cd packages/ts/file-router && npx vitest run
```

All existing tests should pass unchanged, since all features default to `true`.

## 4. Spring Bean Conditional Verification

To verify `@ConditionalOnProperty` works, enable Spring Boot debug output:

```properties
# application.properties
debug=true
hilla.file-router.enabled=false
```

In the startup logs, look for the conditions evaluation report:
```
============================
CONDITIONS EVALUATION REPORT
============================

Negative matches:
-----------------
   RouteUnifyingServiceInitListener:
      Did not match:
         - @ConditionalOnProperty (hilla.file-router.enabled) found different value in property 'hilla.file-router.enabled' (OnPropertyCondition)

   RouteUtilConfiguration:
      Did not match:
         - @ConditionalOnProperty (hilla.file-router.enabled) found different value in property 'hilla.file-router.enabled' (OnPropertyCondition)

   RouteUnifyingConfiguration:
      Did not match:
         - @ConditionalOnProperty (hilla.file-router.enabled) found different value in property 'hilla.file-router.enabled' (OnPropertyCondition)
```

## 5. Edge Cases to Verify

- No configuration file exists -> all features enabled
- Configuration file exists but is empty -> all features enabled (both `.properties` and `.yml`)
- Property set to any value other than `false` -> treated as true
- Property has whitespace: `hilla.file-router.enabled = false ` -> should work (trimmed)
- Multiple features disabled simultaneously
- YAML with quoted string values: `enabled: "false"` -> should work
- YAML with no `hilla:` section -> all features enabled (defaults)
- YAML with mixed content (other Spring properties alongside Hilla) -> only Hilla properties extracted
- Precedence: `.properties` overrides `.yml` when both exist
- Precedence: `.yml` overrides `.yaml` when both exist
- Only `.yaml` present (no `.properties` or `.yml`) -> `.yaml` is used
