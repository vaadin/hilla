# Jackson 2 to Jackson 3 Migration Guide

## Overview
This document describes the migration from Jackson 2 to Jackson 3 for the Hilla framework, required for Spring Boot 4.0.0-M3 compatibility.

## Key Changes

### 1. Maven Group ID Changes
- **Jackson 2**: `com.fasterxml.jackson`
- **Jackson 3**: `tools.jackson`
- **Exception**: `jackson-annotations` remains at `com.fasterxml.jackson.core` with version 2.20

### 2. Package Name Changes
All imports need to be updated:
- `com.fasterxml.jackson.core` → `tools.jackson.core`
- `com.fasterxml.jackson.databind` → `tools.jackson.databind`
- `com.fasterxml.jackson.dataformat` → `tools.jackson.dataformat`
- **Exception**: Annotations remain at `com.fasterxml.jackson.annotation`

### 3. Class Name Changes
Several classes have been renamed in Jackson 3:
- `JsonProcessingException` → `JacksonException`
- `JsonDeserializer` → `ValueDeserializer`
- `JsonSerializer` → `ValueSerializer`
- `SerializerProvider` → `SerializationContext`
- `Module` → `JacksonModule`

### 4. Built-in Modules
Jackson 3 has integrated several modules into `jackson-databind`:
- `jackson-module-parameter-names` - now built-in
- `jackson-datatype-jsr310` (JavaTimeModule) - now built-in
- `jackson-datatype-jdk8` (Jdk8Module) - now built-in

These dependencies can be removed from `pom.xml` files.

### 5. API Changes

#### 5.1 Exception Handling
Methods that previously threw `IOException` no longer do in Jackson 3:
- `deserialize()` methods in custom deserializers
- `serialize()` methods in custom serializers
- `readValue()` calls

**Before (Jackson 2):**
```java
@Override
public MyType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    // implementation
}
```

**After (Jackson 3):**
```java
@Override
public MyType deserialize(JsonParser p, DeserializationContext ctxt) {
    // implementation
}
```

#### 5.2 JsonNode Methods
- `fields()` → `properties()` (returns `Set<Map.Entry<String, JsonNode>>` instead of `Iterator`)
- `fieldNames()` → `propertyNames()` (returns `Collection<String>` instead of `Iterator`)

**Before (Jackson 2):**
```java
node.fields().forEachRemaining(entry -> { ... });
```

**After (Jackson 3):**
```java
for (Map.Entry<String, JsonNode> entry : node.properties()) { ... }
```

#### 5.3 JsonParser Methods
- `getCodec().readTree(p)` → `readValueAsTree()`

**Before (Jackson 2):**
```java
JsonNode node = p.getCodec().readTree(p);
```

**After (Jackson 3):**
```java
JsonNode node = p.readValueAsTree();
```

#### 5.4 DelegatingDeserializer
`DelegatingDeserializer` is now abstract and cannot be instantiated directly.

**Before (Jackson 2):**
```java
var deserializer = new DelegatingDeserializer(converter);
```

**After (Jackson 3):**
```java
var deserializer = new StdConvertingDeserializer<>(converter);
```

#### 5.5 SerializationConfig.introspect() Method
The `introspect()` method has been removed from `SerializationConfig`. You now need to use `ClassIntrospector` to introspect types.

**Before (Jackson 2):**
```java
BeanDescription description = serializationConfig.introspect(javaType);
```

**After (Jackson 3):**
```java
ClassIntrospector introspector = serializationConfig.classIntrospectorInstance();
AnnotatedClass annotatedClass = introspector.introspectClassAnnotations(javaType);
BeanDescription description = introspector.introspectForSerialization(javaType, annotatedClass);
```

#### 5.6 AnnotationIntrospector.isIgnorableType()
The `isIgnorableType()` method now requires a `MapperConfig` parameter as the first argument.

**Before (Jackson 2):**
```java
result = introspector.isIgnorableType(classInfo);
```

**After (Jackson 3):**
```java
result = introspector.isIgnorableType(serializationConfig, classInfo);
```

#### 5.7 ObjectReader.readValue() Method
The `readValue(String, Class)` method has been changed in Jackson 3. You need to use `readerFor()` to specify the type first.

**Before (Jackson 2):**
```java
ObjectReader reader = mapper.reader();
MyType result = reader.readValue(jsonString, MyType.class);
```

**After (Jackson 3):**
```java
ObjectReader reader = mapper.readerFor(MyType.class);
MyType result = reader.readValue(jsonString);
```

### 6. Configuration Changes

#### 6.1 Date/Time Serialization
The configuration for disabling timestamp serialization has changed:

**Before (Jackson 2):**
```java
mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

**Attempted in Jackson 3 (incorrect):**
```java
mapper.disable(tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
```

**Correct Jackson 3:**
```java
mapper.configure(tools.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
```

#### 6.2 ObjectMapper Configuration in Jackson 3
Jackson 3 emphasizes immutability with builders. Configuration should be done via `JsonMapper.builder()`:

**Creating configured ObjectMapper (Jackson 3):**
```java
ObjectMapper mapper = JsonMapper.builder()
    .addModule(new MyModule())
    .addMixIn(TargetClass.class, MixinClass.class)
    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build();
```

**Reconfiguring existing ObjectMapper (Jackson 3):**
```java
// Use rebuild() to reconfigure an existing mapper
JsonMapper reconfigured = ((JsonMapper) mapper).rebuild()
    .addModule(anotherModule)
    .build();
```

**Key differences from Jackson 2:**
- Methods like `addMixIn()` and `registerModule()` are not available on ObjectMapper directly
- Must use builder pattern for configuration
- Use `rebuild()` to modify existing mapper configuration

## Files Modified

### Core Configuration Files
1. **pom.xml** - Updated Jackson version and dependencies
2. **packages/java/endpoint/pom.xml** - Removed obsolete Jackson 2 modules
3. **packages/java/parser-jvm-utils/pom.xml** - Updated dependencies

### Java Source Files
Key files updated with new imports and API changes:
- `EndpointTransferMapper.java` - Updated to use `StdConvertingDeserializer`
- `EndpointControllerConfiguration.java` - Updated date/time configuration
- `JacksonObjectMapperFactory.java` - Now returns Jackson 3 ObjectMapper
- `ByteArrayModule.java` - Removed IOException from method signatures
- `JsonPrinter.java` - Updated exception types and method calls
- `ServerAndClientViewsProvider.java` - Updated JsonNode iteration
- `EndpointInvoker.java` - Updated exception handling and JsonNode iteration
- `OpenAPIUtil.java` - Updated propertyNames() usage

### Test Files
Test files retain Jackson annotation imports (`com.fasterxml.jackson.annotation.*`) as these remain unchanged in Jackson 3.

## Hybrid Jackson 2/3 Approach

Due to Swagger/OpenAPI models requiring Jackson 2 annotations and deserializers, Hilla uses a **hybrid approach**:

- **Jackson 3** (tools.jackson): Used for all Hilla's internal serialization/deserialization
- **Jackson 2** (com.fasterxml.jackson): Used specifically for OpenAPI/Swagger model handling

### Implementation:
1. **OpenApiJackson2Wrapper**: Handles all OpenAPI model deserialization with Jackson 2
2. **OpenAPIFileType**: Uses Swagger's Json/Yaml mappers (Jackson 2) for OpenAPI operations
3. **JsonPrinter**: 
   - Detects OpenAPI objects and uses Jackson 2 for serialization
   - Configured to preserve property/parameter declaration order (disabled alphabetical sorting)
   - Uses Jackson 3 for all non-OpenAPI objects

This approach allows Hilla to be compatible with Spring Boot 4.0 while maintaining Swagger compatibility.

### Required Dependencies:
Even though Jackson 3 has JavaTimeModule and Jdk8Module built-in, we still need to include the Jackson 2 versions of these modules for Swagger:
- `jackson-datatype-jsr310` (2.20.0) - Required by Swagger for JavaTimeModule
- `jackson-datatype-jdk8` (2.20.0) - Required by Swagger for Optional/Stream support

## Migration Status

✅ **Hybrid Migration Complete**: Hilla uses Jackson 3.0.0-rc9 for general operations and Jackson 2 for OpenAPI/Swagger compatibility.

### Latest Fixes (2025-09-26):

1. ✅ Fixed method parameter ordering in OpenAPI generation
   - Disabled alphabetical sorting in JsonPrinter to preserve declaration order
   - Method parameters now maintain their Java declaration order in generated TypeScript
2. ✅ Fixed Jackson 3 compatibility issues in browser-callable finder
   - Resolved duplicate class issues in LookupBrowserCallableFinder
3. ✅ Fixed decimal to integer conversion issues by working around Jackson 3's TreeTraversingParser limitation
   - Added custom coercion logic in EndpointInvoker for JsonNode parameters
   - Handles arrays, collections, and maps containing decimal values
4. ✅ All tests passing with correct property and parameter ordering

### Resolved Issues:
1. ✅ Updated `SerializationConfig.introspect()` to use `ClassIntrospector`
2. ✅ Fixed `AnnotationIntrospector.isIgnorableType()` to include `MapperConfig` parameter
3. ✅ Updated `ObjectReader.readValue()` to use `readerFor()` method
4. ✅ Fixed `MultipartFileCheckerPlugin` to use model accessors instead of direct BeanPropertyDefinition access
5. ✅ Updated all module-info.java files to require `tools.jackson.databind`
6. ✅ Fixed `JsonNode.fields()` to `properties()` for iteration
7. ✅ Fixed `Separators.withObjectFieldValueSpacing()` to `withObjectNameValueSpacing()`
8. ✅ Replaced `JsonProcessingException` with `JacksonException`
9. ✅ Implemented hybrid Jackson 2/3 approach for OpenAPI compatibility

### Key Achievements:
1. Updated Hilla dependencies from `com.fasterxml.jackson` to `tools.jackson`
2. Maintained Jackson 2 for OpenAPI/Swagger model handling
3. Fixed all compilation errors related to API changes
4. Successfully configured date/time serialization without timestamps
5. All modules compile successfully with Jackson 3.0.0-rc9 and Jackson 2 for OpenAPI
6. Compatible with Vaadin Flow 25.0-SNAPSHOT which also uses Jackson 3

## Jackson 3 Default Behavior Changes

### Important Default Changes in Jackson 3
According to the Jackson 3.0 release notes, several default behaviors have changed:

1. **`DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES`**: Default changed from `false` to `true`
   - Jackson 3 will now throw an exception when trying to deserialize `null` into primitive types by default
   - Must explicitly disable this feature to allow null → primitive conversion (null becomes 0 for numeric types, false for boolean)

2. **`DeserializationFeature.READ_ENUMS_USING_TO_STRING`**: Now enabled by default
   - Enums will be deserialized using their `toString()` method by default

3. **Date/Time Formatting**: Jackson 3 uses 'Z' for UTC timezone instead of '+00:00'
   - This is a cosmetic change that doesn't affect functionality

4. **Property Ordering**: Jackson 3 defaults to different property ordering
   - `MapperFeature.SORT_PROPERTIES_ALPHABETICALLY` defaults to `true` in Jackson 3 (was `false` in Jackson 2)
   - This affects both class properties and method parameters in generated JSON
   - **Important**: For OpenAPI generation, this must be disabled to preserve method parameter order
   - Fixed in `JsonPrinter` by explicitly setting both `SORT_PROPERTIES_ALPHABETICALLY` and `ORDER_MAP_ENTRIES_BY_KEYS` to `false`

### Type Coercion Configuration

To maintain compatibility with Jackson 2 behavior for type conversions:

```java
ObjectMapper mapper = JsonMapper.builder()
    .addModule(new ByteArrayModule())
    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
    // Restore Jackson 2 behavior for null to primitive conversion
    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
    // Allow decimal to integer conversion (truncation)
    .enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
    .build();
```

Note: The `ACCEPT_FLOAT_AS_INT` feature controls whether floating-point values can be coerced into integer types. When enabled (default), decimals are truncated (1.9 becomes 1).

**Important limitation**: When deserializing from JsonNode (as opposed to JSON strings), Jackson 3's `TreeTraversingParser` does not respect the `ACCEPT_FLOAT_AS_INT` setting. This affects endpoints that receive JsonNode parameters. A workaround is to convert JsonNode to string before deserialization or handle the coercion manually.

## Recommendations

1. Use `JsonMapper.builder()` pattern for creating configured ObjectMapper instances in Jackson 3
2. Always check for IOException removal when updating custom serializers/deserializers
3. Update iteration patterns when working with JsonNode properties
4. Ensure all Maven dependencies are updated to use the `tools.jackson` group ID (except annotations)
5. Review default behavior changes and adjust configuration accordingly for backward compatibility