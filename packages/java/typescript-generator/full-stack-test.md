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

## Phase 1: Build Full-Stack Testing Infrastructure

### 1.1 Create Node.js Test Runner (Java)
**Location:** `packages/java/typescript-generator/src/test/java/com/vaadin/hilla/parser/testutils/NodeRunner.java`

- Similar to `GeneratorShellRunner` but for tests
- Execute Node.js scripts with proper working directory
- Capture stdout/stderr for debugging
- Throw exceptions on non-zero exit codes

### 1.2 Create Generator Execution Script (Node.js)
**Location:** `packages/java/typescript-generator/src/test/resources/run-generator.mjs`

```javascript
// Reads OpenAPI JSON from stdin or file
// Imports generator packages from packages/ts/generator-*
// Runs generator with specified plugins
// Outputs generated files as JSON map
```

### 1.3 Create Full-Stack Test Helper
**Location:** `packages/java/typescript-generator/src/test/java/com/vaadin/hilla/parser/testutils/FullStackTestHelper.java`

```java
public class FullStackTestHelper {
    // Execute full pipeline: Java → OpenAPI → TypeScript
    public GeneratedFiles executeFullStack(OpenAPI openAPI);

    // Compare generated TS to expected snapshots
    public void assertTypescriptMatches(GeneratedFiles actual, Path snapshotsDir);
}
```

### 1.4 Create TypeScript Comparison Utilities
- Line-by-line diff with clear error messages
- Handle whitespace normalization
- Support for multiple file assertions
- Helper to generate initial snapshots

---

## Phase 2: Convert Existing Java Tests (42 scenarios)

For each existing test (e.g., `SignalTest.java`):

### 2.1 Update Test Method
**Before:**
```java
var openAPI = new Parser()
    .execute(List.of(SignalEndpoint.class));
helper.executeParserWithConfig(openAPI); // Checks openapi.json
```

**After:**
```java
var openAPI = new Parser()
    .execute(List.of(SignalEndpoint.class));
helper.executeFullStack(openAPI); // Generates and checks TypeScript
```

### 2.2 Generate Initial TypeScript Snapshots
**Location pattern:** `src/test/resources/com/vaadin/hilla/parser/.../snapshots/*.ts`

Run tests in "generate mode" to create initial snapshots:
1. Execute TypeScript generator on existing openapi.json
2. Save generated .ts files as snapshots
3. Manually review for correctness

### 2.3 Update TestHelper Usage
- Remove `executeParserWithConfig()` calls
- Replace with `executeFullStack()` + `assertTypescriptMatches()`
- Keep same test structure and assertions for other aspects

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

### 4.1 Create Java Test Class
**Example:** `packages/java/typescript-generator/src/test/java/com/vaadin/hilla/parser/plugins/client/BasicClientTest.java`

```java
@Test
public void should_GenerateBasicClient() {
    var openAPI = resourceLoader.loadOpenAPI("BasicClient.json");
    var generated = helper.executeFullStack(openAPI);
    helper.assertTypescriptMatches(generated,
        resourceLoader.getSnapshotsDir());
}
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
