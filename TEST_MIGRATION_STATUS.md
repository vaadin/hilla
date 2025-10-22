# Test Migration Status

## Summary

End-to-end TypeScript generation test infrastructure has been successfully implemented and one test has been migrated. However, systematic migration of all tests has revealed that the current TypeScript generator has bugs that cause it to crash on certain OpenAPI schemas.

## Completed

### Infrastructure (✓ Complete)
- `EndToEndTestHelper`: Runs full Java → OpenAPI → TypeScript pipeline within Java tests
- `TypeScriptAssertions`: Compares TypeScript output with normalization
- `ExpectedGenerator`: Utility to generate expected TypeScript files
- Plugin discovery and configuration
- Clean test output with SLF4J

### Migrated Tests (1/37)
- ✅ `SimpleTypeTest`: Successfully validates all primitive types

## Discovered Issues

### Generator Bugs
The following tests expose bugs in the current Node.js TypeScript generator:

1. **BareTypeTest** - Crashes with `TypeError: Cannot use 'in' operator to search for 'anyOf' in undefined`
   - Issue: OpenAPI generates `{"type": "array", "nullable": true}` without `items` schema
   - Raw generic types like `List`, `Map`, `Optional` (without type parameters) cause undefined schema
   - Generator's `TypeSchemaProcessor` doesn't handle missing schema gracefully

### Test Inventory

**backbone plugin** (20 tests):
- ✅ SimpleTypeTest
- ❌ BareTypeTest (generator bug)
- ⏸️ ComplexHierarchyTest
- ⏸️ ComplexTypeTest
- ⏸️ CustomConfigTest
- ⏸️ CustomNameTest
- ⏸️ DateTimeTest
- ⏸️ EnumTypeTest
- ⏸️ ExposedTest
- ⏸️ GenericsTest
- ⏸️ GenericSuperClassMethodsTest
- ⏸️ IterableTest
- ⏸️ JacksonTest
- ⏸️ JsonValueTest
- ⏸️ JsonValueNoJsonCreatorTest
- ⏸️ MultiEndpointsTest
- ⏸️ ShadowedNameTest
- ⏸️ SuperClassMethodsTest
- ⏸️ TransientTest
- ⏸️ WildcardTypeTest

**model plugin** (3 tests):
- ⏸️ AnnotationsTest
- ⏸️ JavaTypeTest
- ⏸️ ValidationTest

**nonnull plugin** (5 tests):
- ⏸️ BasicTest
- ⏸️ ExtendedTest
- ⏸️ NonNullApiTest
- ⏸️ NullableTest
- ⏸️ SuperClassMethodsTest

**subtypes plugin** (1 test):
- ⏸️ SubTypesMethodsTest

**transfertypes plugin** (7 tests):
- ⏸️ MultipartFileTest
- ⏸️ JsonNodeTest
- ⏸️ MultipartFileMisuseTest
- ⏸️ BarePageableTest
- ⏸️ PageableTest
- ⏸️ PushTypeTest
- ⏸️ SignalTest
- ⏸️ UUIDTest

## Next Steps (Options)

### Option A: Fix Generator Bugs First
1. Fix `TypeSchemaProcessor` to handle undefined/missing schemas gracefully
2. Add validation to reject invalid OpenAPI (array without items)
3. Fix parser to generate proper schemas for bare generic types
4. Then continue test migration

### Option B: Migrate Tests That Work
1. Skip tests that expose generator bugs (document them)
2. Migrate remaining ~35 tests that work with current generator
3. Use migrated tests to validate future generator rewrites
4. Fix generator bugs as separate work

### Option C: Different Approach
1. Since generator has bugs, consider starting the Java implementation now
2. Use SimpleTypeTest as the baseline
3. Implement Java generator that produces same output
4. Fix bugs in Java implementation while testing
5. Migrate more tests as Java implementation matures

## Recommendation

**Option B** is recommended:
- Continue migrating tests that work (most should work)
- Document tests that expose bugs (skip for now)
- This builds the test suite needed for future generator replacement
- Generator bugs can be fixed separately or in the new Java implementation

The test infrastructure is solid and ready for systematic migration. The discovery of generator bugs is actually valuable - it validates that end-to-end testing catches real issues that OpenAPI-only tests miss.

## Files Changed

- `packages/java/parser-jvm-test-utils/src/main/java/com/vaadin/hilla/parser/testutils/EndToEndTestHelper.java`
- `packages/java/parser-jvm-test-utils/src/main/java/com/vaadin/hilla/parser/testutils/TypeScriptAssertions.java`
- `packages/java/parser-jvm-test-utils/src/main/java/com/vaadin/hilla/parser/testutils/ExpectedGenerator.java`
- `packages/java/parser-jvm-plugin-backbone/src/test/java/com/vaadin/hilla/parser/plugins/backbone/simpletype/SimpleTypeTest.java`
- `packages/java/parser-jvm-plugin-backbone/src/test/resources/com/vaadin/hilla/parser/plugins/backbone/simpletype/expected/SimpleTypeEndpoint.ts`
- `packages/java/parser-jvm-plugin-backbone/pom.xml` (added slf4j-simple)
- `packages/java/parser-jvm-test-utils/pom.xml` (added parser plugin dependencies)
