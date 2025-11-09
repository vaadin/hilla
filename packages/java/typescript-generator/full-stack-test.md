# Plan: Consolidate Generator Tests into Full-Stack Java → TypeScript Testing

## Overview
Transform the testing approach from separate Java→OpenAPI and OpenAPI→TypeScript tests into unified Java→TypeScript full-stack tests in `packages/java/typescript-generator`.

**Current State:**
- **Java tests**: 273 test files, 42 scenarios with openapi.json expectations
- **TypeScript tests**: 24 test files across 11 packages, 21 OpenAPI JSON inputs
- **Split coverage**: Java tests verify Java→OpenAPI, TS tests verify OpenAPI→TypeScript

**Target State:**
- All tests in `packages/java/typescript-generator` verify complete Java→TypeScript pipeline
- OpenAPI JSON is intermediate format only (not verified in tests)
- TypeScript packages remain separate but are used by Java tests
- Zero test coverage loss

---

## Progress Status

**Phase 1: Infrastructure** ✅ COMPLETE + SIMPLIFIED
- ~~NodeRunner.java~~ → Uses Flow's FrontendUtils.executeCommand ✅
- run-generator.mjs ✅
- ~~FullStackTestHelper.java~~ → Merged into AbstractFullStackTest ✅
- TypeScriptComparator.java ✅
- **AbstractFullStackTest.java** ✅ (base class with all configuration)

**Phase 2: Convert Tests** 🔄 IN PROGRESS (4/47 converted)

**Transfertypes Plugin (4/8):**
- ✅ UUIDTest (cleaned: removed connect-client.default.ts, endpoints.ts)
- ✅ JsonNodeTest (cleaned: removed connect-client.default.ts, endpoints.ts)
- ✅ MultipartFileTest (cleaned: removed connect-client.default.ts, endpoints.ts)
- ✅ PushTypeTest (cleaned: removed connect-client.default.ts, endpoints.ts)
- ⏳ PageableTest
- ⏳ BarePageableTest
- ⏳ SignalTest (requires SignalsPlugin fix)
- ⏳ MultipartFileMisuseTest

**Other Plugins (~40 remaining):**
- Backbone: ~20 tests
- Nonnull: ~10 tests
- Model: ~3 tests
- Subtypes: ~1 test

---

## Phase 1: Build Full-Stack Testing Infrastructure

### 1.1 Create Abstract Base Test Class ✅ SIMPLIFIED
**Location:** `packages/java/typescript-generator/src/test/java/com/vaadin/hilla/parser/testutils/AbstractFullStackTest.java`

**Provides all test configuration in one place:**
```java
public abstract class AbstractFullStackTest {
    // Executes Java → OpenAPI → TypeScript pipeline
    // Compares generated TypeScript to snapshots
    // All configuration pre-configured (plugins, classpath, annotations)

    protected void assertTypescriptMatchesSnapshot(Class<?>... endpointClasses)
        throws Exception;
}
```

**Key simplifications:**
- Uses Flow's `FrontendUtils.executeCommand()` instead of custom NodeRunner
- All helper methods integrated (no separate FullStackTestHelper)
- Pre-configured with all plugins (Backbone, TransferTypes, Model, Nonnull, SubTypes, MultipartFileChecker)
- Extended classpath includes Flux and EndpointSubscription
- Both @Endpoint and @EndpointExposed annotations configured

### 1.2 Create Generator Execution Script (Node.js)
**Location:** `packages/java/typescript-generator/src/test/resources/run-generator.mjs`

```javascript
// Reads OpenAPI JSON from stdin or file
// Imports generator packages from packages/ts/generator-*
// Runs generator with specified plugins
// Outputs generated files as JSON map
```

### 1.3 Create TypeScript Comparison Utilities
**Location:** `packages/java/typescript-generator/src/test/java/com/vaadin/hilla/parser/testutils/TypeScriptComparator.java`

- Line-by-line diff with clear error messages
- Handle whitespace normalization
- Support for multiple file assertions
- Helper to generate initial snapshots

---

## Phase 2: Convert Existing Java Tests (42 scenarios)

For each existing test (e.g., `SignalTest.java`):

### 2.1 Update Test Method ✅ SIMPLIFIED

**Before (old split testing):**
```java
var openAPI = new Parser()
    .execute(List.of(SignalEndpoint.class));
helper.executeParserWithConfig(openAPI); // Checks openapi.json
```

**After (with AbstractFullStackTest):**
```java
public class SignalTest extends AbstractFullStackTest {
    @Test
    public void should_GenerateSignalEndpoint() throws Exception {
        assertTypescriptMatchesSnapshot(SignalEndpoint.class);
    }
}
```

**Benefits:**
- No Parser configuration needed (handled by base class)
- No helper instantiation needed
- Single line test assertion
- All plugins automatically included
- Consistent across all tests

### 2.2 Generate Initial TypeScript Snapshots
**Location pattern:** `src/test/resources/com/vaadin/hilla/parser/.../snapshots/*.ts`

**IMPORTANT: Only include snapshots for files relevant to what the test is verifying.**

Run tests in "generate mode" to create initial snapshots:
1. Execute TypeScript generator on existing openapi.json
2. Save generated .ts files as snapshots - **but only files that demonstrate the feature being tested**
3. Manually review for correctness

**Examples:**
- **Transfertypes tests**: Only include endpoint files and generated model files (e.g., `UUIDEndpoint.ts`, `File.ts`)
  - ❌ Do NOT include: `connect-client.default.ts`, `endpoints.ts` (barrel file)
- **Backbone/Client tests**: Include `connect-client.default.ts` since that's what they test
- **Model tests**: Include model files (e.g., `MyModel.ts`)
- **Subtypes tests**: Include model files with type guards

### 2.3 Update Test Structure ✅ SIMPLIFIED
- Extend `AbstractFullStackTest` base class
- Replace entire test implementation with single `assertTypescriptMatchesSnapshot()` call
- All configuration (plugins, classpath, annotations) handled automatically

**Affected files:** ~42 test classes across:
- `plugins/transfertypes/*`
- `plugins/model/*`
- `plugins/subtypes/*`
- `plugins/nonnull/*`
- `plugins/backbone/*`
- `core/security/*`

---

## Phase 3: Inventory and Map Test Coverage

### 3.1 Create Test Mapping Spreadsheet/Document
**Create:** `packages/java/typescript-generator/TEST_MIGRATION.md`

| Java Test | TS Test | Coverage | Action |
|-----------|---------|----------|--------|
| SignalTest | generator-plugin-signals | Both | Merge snapshots |
| PageableTest | generator-plugin-model | Both | Merge snapshots |
| - | generator-plugin-client/BasicClient | TS only | Create Java test |

### 3.2 Identify Gaps
- **Java-only tests**: Scenarios with no TS test → need TS snapshots generated
- **TS-only tests**: Scenarios with no Java test → need Java test created
- **Overlapping tests**: Verify both test same thing, merge snapshots

### 3.3 Cross-Reference Plugin Coverage
Ensure each generator plugin has tests:
- ✓ backbone
- ✓ model
- ✓ client
- ✓ barrel
- ✓ push
- ✓ signals
- ✓ subtypes
- ✓ transfertypes

---

## Phase 4: Migrate TypeScript-Only Tests

For each TS test without Java equivalent (e.g., `BasicClient.spec.ts`):

### 4.1 Create Java Test Class ✅ SIMPLIFIED
**Example:** `packages/java/typescript-generator/src/test/java/com/vaadin/hilla/parser/plugins/client/BasicClientTest.java`

```java
public class BasicClientTest extends AbstractFullStackTest {
    @Test
    public void should_GenerateBasicClient() throws Exception {
        assertTypescriptMatchesSnapshot(BasicClientEndpoint.class);
    }
}
```

**Note:** If you need to load OpenAPI from JSON instead of generating from Java:
```java
// For edge cases where you have OpenAPI JSON but no Java equivalent
var openAPI = resourceLoader.loadOpenAPI("BasicClient.json");
// Then manually call executeFullStack (internal method)
```

### 4.2 Copy OpenAPI Input
**From:** `packages/ts/generator-plugin-client/test/basic/BasicClient.json`
**To:** `packages/java/typescript-generator/src/test/resources/com/vaadin/hilla/parser/plugins/client/BasicClient.json`

### 4.3 Copy/Adapt TypeScript Snapshots
**From:** `packages/ts/generator-plugin-client/test/basic/fixtures/*.snap.ts`
**To:** `packages/java/typescript-generator/src/test/resources/com/vaadin/hilla/parser/plugins/client/snapshots/*.ts`

### 4.4 Affected TS Tests
Migrate these TS-only test scenarios:
- generator-plugin-client tests
- generator-plugin-barrel tests
- generator-core integration tests
- Any plugin-specific edge cases

---

## Phase 5: Verification and Cleanup

### 5.1 Verify Test Coverage Preservation
```bash
# Count before
find packages/ts/generator*/test -name "*.spec.ts" -exec grep -c "it(" {} + | awk '{sum+=$1} END {print sum}'

# Count after
find packages/java/typescript-generator/src/test -name "*Test.java" -exec grep -c "@Test" {} + | awk '{sum+=$1} END {print sum}'
```

Ensure: `after_count >= before_count`

### 5.2 Remove Obsolete OpenAPI JSON Snapshots
```bash
find packages/java/typescript-generator/src/test/resources -name "openapi.json" -delete
```

### 5.3 Update Test Documentation
**Create/Update:** `packages/java/typescript-generator/README.md`
- Document full-stack testing approach
- Explain snapshot generation process
- Provide debugging tips for failures

### 5.4 Run Full Test Suite
```bash
mvn test -pl :hilla-typescript-generator
```

Verify all tests pass.

### 5.5 Mark TypeScript Generator Packages as Test-Only
Update `packages/ts/generator-*/package.json` to indicate they're consumed by Java tests.

---

## Phase 6: Future TypeScript Test Migration (Optional)

After Java tests are stable, optionally remove TypeScript test packages:
- Keep generator-* source code
- Remove test/ directories
- Update package.json scripts
- Document that testing happens in Java

---

## Critical Success Criteria

✅ **No test loss**: Every existing test scenario preserved
✅ **Full-stack coverage**: All tests verify Java → TypeScript
✅ **Snapshot accuracy**: Expected outputs match actual generator behavior
✅ **Clear failures**: Test failures clearly indicate what differs
✅ **Maintainable**: Easy to add new tests, update snapshots

---

## Rollback Plan

If issues arise:
1. Git branch preserves original state
2. Can revert to split testing temporarily
3. Phase-by-phase approach allows incremental validation

---

## Estimated Effort

- Phase 1 (Infrastructure): 2-3 days
- Phase 2 (Convert Java tests): 3-4 days
- Phase 3 (Inventory): 1 day
- Phase 4 (Migrate TS tests): 2-3 days
- Phase 5 (Verification): 1-2 days

**Total: ~10-14 days** (with careful validation at each phase)

---

## Implementation Notes

### User Preferences (from planning session)
- **OpenAPI verification**: Skip - only check TypeScript output
- **TS invocation**: Via direct Node.js script importing generator packages
- **Snapshot updates**: Manual - developers update when intentional changes occur
- **Code location**: Keep TS packages separate in packages/ts/generator-*

---

## Best Practices for Writing Full-Stack Tests

### 0. Use AbstractFullStackTest Base Class ✅

**All tests should extend AbstractFullStackTest:**
```java
public class MyFeatureTest extends AbstractFullStackTest {
    @Test
    public void should_VerifyMyFeature() throws Exception {
        assertTypescriptMatchesSnapshot(MyEndpoint.class);
    }
}
```

**The base class provides:**
- ✅ All plugins pre-configured (Backbone, TransferTypes, Model, Nonnull, SubTypes, MultipartFileChecker)
- ✅ Extended classpath with Flux and EndpointSubscription
- ✅ Both @Endpoint and @EndpointExposed annotations
- ✅ Automatic Node.js execution via Flow's FrontendUtils
- ✅ Snapshot comparison with clear error messages

**Test complexity reduction:**
- Before: ~60 lines per test (manual Parser setup, helper usage)
- After: ~15 lines per test (just extend base class and assert)

### 1. Keep Snapshots Minimal and Focused

**DO:**
- ✅ Only include snapshots that demonstrate the specific feature being tested
- ✅ For transfertypes tests: include endpoint files and any generated model files
- ✅ For model tests: include the model TypeScript files
- ✅ For client/backbone tests: include `connect-client.default.ts`
- ✅ For subtypes tests: include model files with type guards

**DON'T:**
- ❌ Include `connect-client.default.ts` in every test
- ❌ Include `endpoints.ts` barrel files unless testing barrel generation
- ❌ Include unrelated generated files

**Why?** Keeping snapshots minimal makes tests:
- Easier to understand (clear what's being tested)
- Faster to run (less file I/O and comparison)
- Easier to maintain (fewer files to update when generator changes)
- More focused (failures point to actual issues, not incidental changes)

### 2. Test Names Should Clearly Indicate What's Being Verified

**Examples:**
- `UUIDFullStackTest` → verifies UUID → string transformation
- `MultipartFileFullStackTest` → verifies MultipartFile → File transformation
- `SignalsFullStackTest` → verifies Signals support in endpoints

### 3. Snapshot Directory Structure

```
src/test/resources/com/vaadin/hilla/parser/plugins/{plugin}/{feature}/
├── snapshots/
│   ├── MyEndpoint.ts                      # Endpoint client methods
│   ├── MyModel.ts                         # Generated models (if relevant)
│   └── com/vaadin/hilla/runtime/...      # Runtime types (if relevant)
└── (test Java files reference these snapshots)
```

### 4. When to Include Common Files

**`connect-client.default.ts`**: Only in tests that verify:
- Client generation behavior
- Backbone plugin functionality
- Authentication/authorization in clients

**`endpoints.ts`**: Only in tests that verify:
- Barrel file generation
- Export structure

**Model files**: Include when testing:
- Transfer types (UUID, JsonNode, File, etc.)
- Model generation and structure
- Polymorphic types (subtypes)
- Nullability annotations

---

## Architecture Improvements and Simplifications

### Refactoring Summary (3 commits)

The initial infrastructure was simplified through 3 major refactorings:

**1. Commit: Create AbstractFullStackTest base class**
- Unified all test configuration in single base class
- Eliminated boilerplate from test files
- Pre-configured all plugins, classpath, and annotations
- Result: -200 lines across test files

**2. Commit: Merge FullStackTestHelper into AbstractFullStackTest**
- Removed unnecessary helper layer
- All functionality integrated into base class
- Cleaner architecture with single responsibility
- Result: -39 lines (FullStackTestHelper removed)

**3. Commit: Replace NodeRunner with Flow's FrontendUtils.executeCommand**
- **Eliminated custom ProcessBuilder implementation entirely**
- **Uses existing Flow infrastructure (FrontendUtils.executeCommand)**
- Platform-aware command execution (Windows/Unix)
- Proper stream handling and error reporting
- Result: **-197 lines (NodeRunner removed)**

### Net Impact

**Code reduction: -436 lines**
- Deleted: 557 lines of boilerplate and custom infrastructure
- Added: 121 lines (AbstractFullStackTest base class)

**Test simplification:**
```java
// Before: 63 lines
public class UUIDFullStackTest {
    private final FullStackTestHelper helper = new FullStackTestHelper(getClass());

    @Test
    public void should_ReplaceUUIDClassWithStringInTypeScript()
            throws IOException, URISyntaxException, FullStackExecutionException {
        var openAPI = new Parser()
            .classPath(Set.of(helper.getTargetDir().toString()))
            .endpointAnnotations(List.of(Endpoint.class))
            .endpointExposedAnnotations(List.of(EndpointExposed.class))
            .addPlugin(new BackbonePlugin())
            .addPlugin(new TransferTypesPlugin())
            .execute(List.of(UUIDEndpoint.class));
        var generated = helper.executeFullStack(openAPI);
        helper.assertTypescriptMatches(generated, helper.getSnapshotsDir());
    }
}

// After: 33 lines (48% reduction)
public class UUIDTest extends AbstractFullStackTest {
    @Test
    public void should_ReplaceUUIDClassWithStringInTypeScript() throws Exception {
        assertTypescriptMatchesSnapshot(UUIDEndpoint.class);
    }
}
```

### Key Architectural Benefits

1. **Reuses existing infrastructure**: No custom ProcessBuilder code, uses Flow's battle-tested FrontendUtils
2. **Single source of truth**: All test configuration in AbstractFullStackTest
3. **Consistent testing**: All tests use same plugins, classpath, and annotations
4. **Easy to maintain**: Changes to configuration only need to happen in one place
5. **Simple to write**: New tests are 3 lines (class + @Test + assertion)

### Files Structure

**Current infrastructure (as of refactoring):**
```
packages/java/typescript-generator/src/test/java/com/vaadin/hilla/parser/testutils/
├── AbstractFullStackTest.java        ← All-in-one base class (uses FrontendUtils)
├── TypeScriptComparator.java         ← File comparison logic
└── ResourceLoader.java                ← Test resource utilities

packages/java/typescript-generator/src/test/resources/
└── run-generator.mjs                  ← Node.js script (called by AbstractFullStackTest)
```

**Removed files (no longer needed):**
- ~~NodeRunner.java~~ → Flow's FrontendUtils.executeCommand
- ~~FullStackTestHelper.java~~ → Merged into AbstractFullStackTest

### Usage

Writing a new full-stack test is now trivial:

```java
public class MyNewFeatureTest extends AbstractFullStackTest {
    @Test
    public void should_GenerateMyFeature() throws Exception {
        assertTypescriptMatchesSnapshot(MyEndpoint.class);
    }
}
```

That's it! No configuration, no setup, no boilerplate.
