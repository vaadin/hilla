/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package dev.hilla.generator.endpoints;

import javax.annotation.security.DenyAll;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.hilla.Endpoint;
import dev.hilla.EndpointExposed;
import dev.hilla.ExplicitNullableTypeChecker;
import dev.hilla.auth.EndpointAccessChecker;
import dev.hilla.endpointransfermapper.EndpointTransferMapper;
import dev.hilla.generator.OpenAPIObjectGenerator;
import dev.hilla.mappedtypes.Pageable;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.parser.OpenAPIV3Parser;
import reactor.core.publisher.Flux;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;

import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import dev.hilla.generator.endpoints.complexhierarchymodel.GrandParentModel;
import dev.hilla.generator.endpoints.complexhierarchymodel.Model;
import dev.hilla.generator.endpoints.complexhierarchymodel.ParentModel;
import dev.hilla.utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractEndpointGenerationTest
        extends AbstractEndpointGeneratorBaseTest {
    private static final List<Class<?>> JSON_NUMBER_CLASSES = Arrays.asList(
            Number.class, byte.class, char.class, short.class, int.class,
            long.class, float.class, double.class);
    private static final Pattern JAVA_PATH_REFERENCE_REGEX = Pattern
            .compile("( \\* @see \\{@link file:\\/\\/(.*)\\}\r?\n)");

    /**
     * Classes in this list are simulated as classes from different jars so that
     * it doesn't have absolute link to the original java file.
     */
    private static final List<Class> DENY_LIST_CHECKING_ABSOLUTE_PATH = Arrays
            .asList(Model.class, ParentModel.class, GrandParentModel.class);
    private static final EndpointAccessChecker accessChecker = new EndpointAccessChecker(
            new AccessAnnotationChecker());
    private final Set<String> schemaReferences = new HashSet<>();

    public AbstractEndpointGenerationTest(List<Class<?>> testClasses) {
        super(testClasses);
    }

    protected void verifyOpenApiObjectAndGeneratedTs() {
        generateAndVerify(null, null);
    }

    protected void verifyGenerationFully(URL customApplicationProperties,
            URL expectedOpenApiJsonResourceUrl) {
        generateAndVerify(customApplicationProperties, Objects.requireNonNull(
                expectedOpenApiJsonResourceUrl,
                "Full verification requires an expected open api spec file"));
    }

    private void generateAndVerify(URL customApplicationProperties,
            URL expectedOpenApiJsonResourceUrl) {

        generateOpenApi(customApplicationProperties);

        Assert.assertTrue(
                String.format("No generated json found at path '%s'",
                        openApiJsonOutput),
                openApiJsonOutput.toFile().exists());

        verifyOpenApiObject();
        if (expectedOpenApiJsonResourceUrl != null) {
            verifyOpenApiJson(expectedOpenApiJsonResourceUrl);
        }

        generateTsEndpoints();

        verifyTsModule();
        verifyModelTsModule();
    }

    private void verifyOpenApiObject() {
        OpenAPI actualOpenAPI = getOpenApiObject();
        assertPaths(actualOpenAPI.getPaths(), endpointClasses);

        if (!nonEndpointClasses.isEmpty()) {
            assertComponentSchemas(actualOpenAPI.getComponents().getSchemas(),
                    nonEndpointClasses);
        } else {
            Map<String, Schema> componentSchemas = Optional
                    .ofNullable(actualOpenAPI.getComponents())
                    .map(Components::getSchemas).orElse(Collections.emptyMap());

            removeMapperClasses(componentSchemas);
            assertTrue(String.format(
                    "Got schemas that correspond to no class provided in test parameters, schemas: '%s'",
                    componentSchemas), componentSchemas.isEmpty());
        }

        verifySchemaReferences();
    }

    private void removeMapperClasses(Map<String, Schema> componentSchemas) {
        componentSchemas.keySet().removeIf(clsName -> {
            /* Skip classes that are added because of the mappers */
            if (clsName
                    .startsWith(Pageable.class.getPackage().getName() + ".")) {
                return true;
            }
            if (Direction.class.getCanonicalName().equals(clsName)
                    || NullHandling.class.getCanonicalName().equals(clsName)) {
                return true;
            }
            return false;
        });
    }

    private void assertPaths(Paths actualPaths,
            List<Class<?>> testEndpointClasses) {
        int pathCount = 0;
        for (Class<?> testEndpointClass : testEndpointClasses) {
            pathCount += assertClassPathsRecursive(actualPaths,
                    testEndpointClass, testEndpointClass, new HashMap<>());
        }
        assertEquals("Unexpected number of OpenAPI paths found", pathCount,
                actualPaths.size());
    }

    private Class<?> applyTypeArguments(Type type,
            HashMap<String, Class<?>> typeArguments) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return applyTypeArguments(((ParameterizedType) type).getRawType(),
                    typeArguments);
        } else if (type instanceof GenericArrayType) {
            return Array.newInstance(applyTypeArguments(
                    ((GenericArrayType) type).getGenericComponentType(),
                    typeArguments)).getClass();
        } else if (type instanceof TypeVariable) {
            Class<?> argument = typeArguments
                    .get(((TypeVariable<?>) type).getName());
            if (argument != null) {
                return argument;
            }
        }

        return Object.class;
    }

    private HashMap<String, Class<?>> extractTypeArguments(Type type,
            HashMap<String, Class<?>> parentTypeArguments) {
        HashMap<String, Class<?>> typeArguments = new HashMap<>();
        if (!(type instanceof ParameterizedType)) {
            return typeArguments;
        }

        ParameterizedType parameterizedType = ((ParameterizedType) type);

        TypeVariable[] typeVariables = ((Class<?>) parameterizedType
                .getRawType()).getTypeParameters();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        for (int i = 0; i < typeVariables.length; i++) {
            typeArguments.put(typeVariables[i].getName(), applyTypeArguments(
                    actualTypeArguments[i], parentTypeArguments));
        }

        return typeArguments;
    }

    private int assertClassPathsRecursive(Paths actualPaths,
            Class<?> testEndpointClass, Class<?> testMethodsClass,
            HashMap<String, Class<?>> typeArguments) {
        if (!testMethodsClass.equals(testEndpointClass) && !testMethodsClass
                .isAnnotationPresent(EndpointExposed.class)) {
            return 0;
        }

        int pathCount = 0;
        for (Method expectedEndpointMethod : testMethodsClass
                .getDeclaredMethods()) {
            if (!Modifier.isPublic(expectedEndpointMethod.getModifiers())
                    || accessChecker.getAccessAnnotationChecker()
                            .getSecurityTarget(expectedEndpointMethod)
                            .isAnnotationPresent(DenyAll.class)) {
                continue;
            }
            pathCount++;
            String expectedEndpointUrl = String.format("/%s/%s",
                    getEndpointName(testEndpointClass),
                    expectedEndpointMethod.getName());
            PathItem actualPath = actualPaths.get(expectedEndpointUrl);
            assertNotNull(String.format(
                    "Expected to find a path '%s' for the endpoint method '%s' in the class '%s'",
                    expectedEndpointUrl, expectedEndpointMethod,
                    testEndpointClass), actualPath);
            assertPath(testEndpointClass, expectedEndpointMethod, actualPath,
                    typeArguments);
        }

        Type genericSuperClass = testMethodsClass.getGenericSuperclass();
        pathCount += assertClassPathsRecursive(actualPaths, testEndpointClass,
                applyTypeArguments(genericSuperClass, typeArguments),
                extractTypeArguments(genericSuperClass, typeArguments));

        for (Type genericInterface : testMethodsClass.getGenericInterfaces()) {
            pathCount += assertClassPathsRecursive(actualPaths,
                    testEndpointClass,
                    applyTypeArguments(genericInterface, typeArguments),
                    extractTypeArguments(genericInterface, typeArguments));
        }

        return pathCount;
    }

    private String getEndpointName(Class<?> testEndpointClass) {
        String customName = testEndpointClass.getAnnotation(Endpoint.class)
                .value();
        return customName.isEmpty() ? testEndpointClass.getSimpleName()
                : customName;
    }

    private void assertPath(Class<?> testEndpointClass,
            Method expectedEndpointMethod, PathItem actualPath,
            HashMap<String, Class<?>> typeArguments) {
        Operation actualOperation = actualPath.getPost();
        assertEquals("Unexpected tag in the OpenAPI spec",
                Collections.singletonList(testEndpointClass.getSimpleName()),
                actualOperation.getTags());
        assertTrue(String.format(
                "Unexpected OpenAPI operation id: does not contain the endpoint name of the class '%s'",
                testEndpointClass.getSimpleName()),
                actualOperation.getOperationId()
                        .contains(getEndpointName(testEndpointClass)));
        assertTrue(String.format(
                "Unexpected OpenAPI operation id: does not contain the name of the endpoint method '%s'",
                expectedEndpointMethod.getName()),
                actualOperation.getOperationId()
                        .contains(expectedEndpointMethod.getName()));

        if (expectedEndpointMethod.getParameterCount() > 0) {
            Schema requestSchema = extractSchema(
                    actualOperation.getRequestBody().getContent());
            Type[] genericParameterTypes = expectedEndpointMethod
                    .getGenericParameterTypes();
            Class<?>[] parameterTypes = new Class<?>[genericParameterTypes.length];
            List<HashMap<String, Class<?>>> parameterTypeArguments = new ArrayList<>();
            for (int i = 0; i < genericParameterTypes.length; i++) {
                parameterTypes[i] = applyTypeArguments(genericParameterTypes[i],
                        typeArguments);
                parameterTypeArguments.add(i, extractTypeArguments(
                        genericParameterTypes[i], typeArguments));
            }
            assertRequestSchema(requestSchema, parameterTypes,
                    parameterTypeArguments,
                    expectedEndpointMethod.getParameters());
        } else {
            assertNull(String.format(
                    "No request body should be present in path schema for endpoint method with no parameters, method: '%s'",
                    expectedEndpointMethod), actualOperation.getRequestBody());
        }

        ApiResponses responses = actualOperation.getResponses();
        assertEquals(
                "Every operation is expected to have a single '200' response",
                1, responses.size());
        ApiResponse apiResponse = responses.get("200");
        assertNotNull(
                "Every operation is expected to have a single '200' response",
                apiResponse);

        Type genericReturnType = expectedEndpointMethod.getGenericReturnType();
        Class<?> returnType = applyTypeArguments(genericReturnType,
                typeArguments);
        Class<?> mappedType = new EndpointTransferMapper()
                .getTransferType(returnType);
        if (mappedType != null) {
            returnType = mappedType;
        }
        if (returnType != void.class) {
            assertSchema(extractSchema(apiResponse.getContent()), returnType,
                    extractTypeArguments(genericReturnType, typeArguments));
        } else {
            assertNull(String.format(
                    "No response is expected to be present for void method '%s'",
                    expectedEndpointMethod), apiResponse.getContent());
        }

        assertNotNull(
                "Non-anonymous endpoint method should have a security data defined for it in the schema",
                actualOperation.getSecurity());
    }

    private void assertRequestSchema(Schema requestSchema,
            Class<?>[] parameterTypes,
            List<HashMap<String, Class<?>>> parameterTypeArguments,
            Parameter[] parameters) {
        Map<String, Schema> properties = requestSchema.getProperties();
        assertEquals(
                "Request schema should have the same amount of properties as the corresponding endpoint method parameters number",
                parameterTypes.length, properties.size());
        int index = 0;
        for (Map.Entry<String, Schema> stringSchemaEntry : properties
                .entrySet()) {
            assertSchema(stringSchemaEntry.getValue(), parameterTypes[index],
                    parameterTypeArguments.get(index));
            List requiredList = requestSchema.getRequired();
            assertTrue(requiredList.contains(stringSchemaEntry.getKey()));
            index++;
        }
    }

    private Schema extractSchema(Content content) {
        assertEquals("Expecting a single application content — a json schema",
                1, content.size());
        return content.get("application/json").getSchema();
    }

    private void assertComponentSchemas(Map<String, Schema> actualSchemas,
            List<Class<?>> testEndpointClasses) {
        int schemasCount = 0;
        for (Class<?> expectedSchemaClass : testEndpointClasses) {
            schemasCount++;
            Schema actualSchema = actualSchemas
                    .get(expectedSchemaClass.getCanonicalName());
            assertNotNull(String.format(
                    "Expected to have a schema defined for a class '%s'",
                    expectedSchemaClass), actualSchema);
            assertSchema(actualSchema, expectedSchemaClass, new HashMap<>());
        }
        assertEquals("Expected to have all endpoint classes defined in schemas",
                schemasCount, actualSchemas.size());
    }

    private void assertSchema(Schema actualSchema, Class<?> expectedSchemaClass,
            HashMap<String, Class<?>> typeArguments) {
        if (assertSpecificJavaClassSchema(actualSchema, expectedSchemaClass,
                typeArguments)) {
            return;
        }

        if (actualSchema.get$ref() != null) {
            assertNull(actualSchema.getProperties());
            schemaReferences.add(actualSchema.get$ref());
        } else {
            if (actualSchema instanceof StringSchema) {
                assertTrue(String.class.isAssignableFrom(expectedSchemaClass)
                        || Enum.class.isAssignableFrom(expectedSchemaClass));
            } else if (actualSchema instanceof BooleanSchema) {
                assertTrue((boolean.class.isAssignableFrom(expectedSchemaClass)
                        || Boolean.class
                                .isAssignableFrom(expectedSchemaClass)));
            } else if (actualSchema instanceof NumberSchema) {
                assertTrue(JSON_NUMBER_CLASSES.stream()
                        .anyMatch(jsonNumberClass -> jsonNumberClass
                                .isAssignableFrom(expectedSchemaClass)));
            } else if (actualSchema instanceof ArraySchema) {
                if (expectedSchemaClass == Flux.class) {
                    assertSchema(((ArraySchema) actualSchema).getItems(),
                            typeArguments.values().iterator().next(),
                            typeArguments);
                } else if (expectedSchemaClass.isArray()) {
                    assertSchema(((ArraySchema) actualSchema).getItems(),
                            expectedSchemaClass.getComponentType(),
                            typeArguments);
                } else {
                    assertTrue(Iterable.class
                            .isAssignableFrom(expectedSchemaClass));
                    Type itemType = expectedSchemaClass.getTypeParameters()[0];
                    assertSchema(((ArraySchema) actualSchema).getItems(),
                            applyTypeArguments(itemType, typeArguments),
                            new HashMap<>());
                }
            } else if (actualSchema instanceof MapSchema) {
                assertTrue(Map.class.isAssignableFrom(expectedSchemaClass));
            } else if (actualSchema instanceof DateTimeSchema) {
                assertTrue(Instant.class.isAssignableFrom(expectedSchemaClass)
                        || LocalDateTime.class
                                .isAssignableFrom(expectedSchemaClass)
                        || LocalTime.class
                                .isAssignableFrom(expectedSchemaClass));
            } else if (actualSchema instanceof DateSchema) {
                assertTrue(Date.class.isAssignableFrom(expectedSchemaClass)
                        || LocalDate.class
                                .isAssignableFrom(expectedSchemaClass));
            } else if (actualSchema instanceof ComposedSchema) {
                List<Schema> allOf = ((ComposedSchema) actualSchema).getAllOf();
                if (allOf.size() > 1) {
                    // Inherited schema
                    for (Schema schema : allOf) {
                        if (expectedSchemaClass.getCanonicalName()
                                .equals(schema.getName())) {
                            assertSchemaProperties(expectedSchemaClass,
                                    typeArguments, schema);
                            break;
                        }
                    }
                } else {
                    // Nullable schema for referring schema object
                    assertEquals(1, allOf.size());
                    String expectedName = expectedSchemaClass
                            .getCanonicalName();
                    Class<?> transferType = new EndpointTransferMapper()
                            .getTransferType(expectedSchemaClass);
                    if (transferType != null) {
                        expectedName = transferType.getCanonicalName();
                        ;
                    }
                    assertEquals(expectedName, allOf.get(0).getName());
                }
            } else if (actualSchema instanceof ObjectSchema) {
                assertSchemaProperties(expectedSchemaClass, typeArguments,
                        actualSchema);
            } else {
                throw new AssertionError(
                        String.format("Unknown schema '%s' for class '%s'",
                                actualSchema.getClass(), expectedSchemaClass));
            }
        }
    }

    private boolean assertSpecificJavaClassSchema(Schema actualSchema,
            Class<?> expectedSchemaClass,
            HashMap<String, Class<?>> typeArguments) {
        if (expectedSchemaClass == Optional.class) {
            if (actualSchema instanceof ComposedSchema) {
                ComposedSchema actualComposedSchema = (ComposedSchema) actualSchema;
                assertEquals(1, actualComposedSchema.getAllOf().size());
                assertSchema(((ComposedSchema) actualSchema).getAllOf().get(0),
                        typeArguments
                                .get(expectedSchemaClass.getTypeParameters()[0]
                                        .getName()),
                        new HashMap<>());
            }
        } else if (expectedSchemaClass == Object.class) {
            assertNull(actualSchema.getProperties());
            assertNull(actualSchema.getAdditionalProperties());
            assertNull(actualSchema.get$ref());
            assertNull(actualSchema.getRequired());
        } else {
            return false;
        }
        return true;
    }

    private void assertSchemaProperties(Class<?> expectedSchemaClass,
            HashMap<String, Class<?>> typeArguments, Schema schema) {
        int expectedFieldsCount = 0;
        Map<String, Schema> properties = schema.getProperties();
        assertNotNull(properties);
        assertTrue(properties.size() > 0);
        for (Field expectedSchemaField : expectedSchemaClass
                .getDeclaredFields()) {
            if (Modifier.isTransient(expectedSchemaField.getModifiers())
                    || Modifier.isStatic(expectedSchemaField.getModifiers())
                    || expectedSchemaField
                            .isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }

            expectedFieldsCount++;
            Schema propertySchema = properties
                    .get(expectedSchemaField.getName());
            assertNotNull(String.format("Property schema is not found %s",
                    expectedSchemaField.getName()), propertySchema);
            Type type = expectedSchemaField.getGenericType();
            assertSchema(propertySchema, expectedSchemaField.getType(),
                    extractTypeArguments(type, typeArguments));
            if (ExplicitNullableTypeChecker.isRequired(expectedSchemaField)) {
                assertTrue(schema.getRequired()
                        .contains(expectedSchemaField.getName()));
            } else {
                boolean notRequired = schema.getRequired() == null || !schema
                        .getRequired().contains(expectedSchemaField.getName());
                assertTrue(notRequired);
            }
        }
        assertEquals(expectedFieldsCount, properties.size());

    }

    private void verifySchemaReferences() {
        nonEndpointClasses.stream().map(Class::getCanonicalName)
                .forEach(schemaClass -> schemaReferences.removeIf(ref -> ref
                        .endsWith(String.format("/%s", schemaClass))));
        String errorMessage = String.format(
                "Got schema references that are not in the OpenAPI schemas: '%s'",
                StringUtils.join(schemaReferences, ","));
        Assert.assertTrue(errorMessage, schemaReferences.isEmpty());
    }

    private void verifyOpenApiJson(URL expectedOpenApiJsonResourceUrl) {
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        OpenAPI generated = parser
                .read(openApiJsonOutput.toAbsolutePath().toString());
        try {
            OpenAPI expected = parser
                    .read(new File(expectedOpenApiJsonResourceUrl.toURI())
                            .toPath().toAbsolutePath().toString());

            removeAndCompareFilePathExtensionInTags(generated, expected);
            removeAndCompareFilePathExtensionInSchemas(generated, expected);
            sortTagsAndSchemas(generated);
            sortTagsAndSchemas(expected);

            TestUtils.equalsIgnoreWhiteSpaces(
                    "The generated OpenAPI does not match", expected.toString(),
                    generated.toString());

        } catch (URISyntaxException e) {
            throw new IllegalStateException("Can't compare OpenAPI json.", e);
        }
    }

    private void sortTagsAndSchemas(OpenAPI api) {
        // sort tags
        api.getTags().sort(Comparator.comparing(Tag::getName));
        // sort component schemas
        api.getComponents()
                .setSchemas(new TreeMap<>(api.getComponents().getSchemas()));
    }

    private void removeAndCompareFilePathExtensionInSchemas(OpenAPI generated,
            OpenAPI expected) {
        Map<String, String> generatedSchemaAndFilePathMap = new HashMap<>();
        getSchemaNameAndFilePathMap(generated, generatedSchemaAndFilePathMap);

        Map<String, String> expectedSchemaAndFilePathMap = new HashMap<>();
        getSchemaNameAndFilePathMap(expected, expectedSchemaAndFilePathMap);

        for (Map.Entry<String, String> stringStringEntry : generatedSchemaAndFilePathMap
                .entrySet()) {
            String key = stringStringEntry.getKey();
            String value = stringStringEntry.getValue();
            boolean isBothNull = value == null
                    && expectedSchemaAndFilePathMap.get(key) == null;
            String errorMessage = String.format(
                    "File path doesn't match " + "for schema '%s'", key);

            String ending = expectedSchemaAndFilePathMap.get(key).replace('/',
                    File.separatorChar);
            Assert.assertTrue(errorMessage,
                    isBothNull || (value != null && value.endsWith(ending)));
        }
    }

    private void getSchemaNameAndFilePathMap(OpenAPI openAPI,
            Map<String, String> schemaNameAndFilePathMap) {
        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
        for (Map.Entry<String, Schema> stringSchemaEntry : schemas.entrySet()) {
            if (stringSchemaEntry.getValue().getExtensions() != null) {
                schemaNameAndFilePathMap.put(stringSchemaEntry.getKey(),
                        (String) stringSchemaEntry.getValue().getExtensions()
                                .get(OpenAPIObjectGenerator.EXTENSION_VAADIN_FILE_PATH));
                stringSchemaEntry.getValue().getExtensions().remove(
                        OpenAPIObjectGenerator.EXTENSION_VAADIN_FILE_PATH);
            }
        }
    }

    private void removeAndCompareFilePathExtensionInTags(OpenAPI generated,
            OpenAPI expected) {
        Map<String, String> generatedFilePath = new HashMap<>();
        List<Tag> newTagsWithoutFilePath = mapTagNameWithPath(generated,
                generatedFilePath);
        generated.setTags(newTagsWithoutFilePath);

        Map<String, String> expectedFilePath = new HashMap<>();
        List<Tag> expectedTagsWithoutFilePath = mapTagNameWithPath(expected,
                expectedFilePath);
        expected.setTags(expectedTagsWithoutFilePath);

        for (Map.Entry<String, String> generatedEntrySet : generatedFilePath
                .entrySet()) {
            String value = generatedEntrySet.getValue();
            String key = generatedEntrySet.getKey();
            boolean isBothNull = value == null
                    && expectedFilePath.get(key) == null;
            String errorMessage = String
                    .format("File path doesn't match for tag '%s'", key);
            String ending = expectedFilePath.get(key).replace('/',
                    File.separatorChar);
            Assert.assertTrue(errorMessage,
                    isBothNull || (value != null && value.endsWith(ending)));
        }
    }

    private List<Tag> mapTagNameWithPath(OpenAPI openAPI,
            Map<String, String> tagNameFilePathMap) {
        return openAPI.getTags().stream().peek(tag -> {
            if (tag.getExtensions() != null) {
                tagNameFilePathMap.put(tag.getName(), (String) tag
                        .getExtensions()
                        .get(OpenAPIObjectGenerator.EXTENSION_VAADIN_FILE_PATH));
                tag.getExtensions().remove(
                        OpenAPIObjectGenerator.EXTENSION_VAADIN_FILE_PATH);
            }
        }).collect(Collectors.toList());
    }

    private void verifyOpenApiJsonByString(URL expectedOpenApiJsonResourceUrl) {
        assertEquals(TestUtils.readResource(expectedOpenApiJsonResourceUrl),
                readFile(openApiJsonOutput));
    }

    private void verifyTsModule() {
        List<File> foundFiles = getTsFiles(outputDirectory.getRoot());
        assertEquals(String.format(
                "Expected to have only %s classes processed in the test '%s', but found the following files: '%s'",
                endpointClasses.size(), endpointClasses, foundFiles),
                endpointClasses.size(), foundFiles.size());
        for (Class<?> expectedClass : endpointClasses) {
            assertClassGeneratedTs(expectedClass);
        }
    }

    private void verifyModelTsModule() {
        nonEndpointClasses.forEach(this::assertModelClassGeneratedTs);
    }

    private void assertClassGeneratedTs(Class<?> expectedClass) {
        String classResourceUrl = String.format("expected-%s.ts",
                expectedClass.getSimpleName());
        URL expectedResource = this.getClass().getResource(classResourceUrl);
        Assert.assertNotNull(String.format("Expected file is not found at %s",
                classResourceUrl), expectedResource);
        String expectedTs = TestUtils.readResource(expectedResource);

        Path outputFilePath = outputDirectory.getRoot().toPath()
                .resolve(expectedClass.getSimpleName() + ".ts");
        String errorMessage = String.format(
                "Class '%s' has unexpected json produced in file '%s'",
                expectedClass, expectedResource.getPath());
        String actualContent = readFile(outputFilePath);
        if (!expectedClass.getPackage().getName().startsWith("dev.hilla")) {
            // the class comes from jars
            Assert.assertEquals(errorMessage, expectedTs, actualContent);
            return;
        }
        removeAbsolutePathAndCompare(expectedClass, expectedTs, errorMessage,
                actualContent);
    }

    private void assertModelClassGeneratedTs(Class<?> expectedClass) {
        String canonicalName = expectedClass.getCanonicalName();
        String expectedFileName = constructExpectedModelFileName(expectedClass);
        URL expectedResource = this.getClass().getResource(expectedFileName);
        Assert.assertNotNull(String.format("Expected file is not found at %s",
                expectedFileName), expectedResource);
        String expectedTs = TestUtils.readResource(expectedResource);

        Path outputFilePath = outputDirectory.getRoot().toPath().resolve(
                StringUtils.replaceChars(canonicalName, '.', '/') + ".ts");

        String errorMessage = String.format(
                "Model class '%s' has unexpected typescript produced in file '%s'",
                expectedClass, expectedResource.getPath());
        String actualContent = readFile(outputFilePath);
        if (!expectedClass.getPackage().getName().startsWith("dev.hilla")
                || DENY_LIST_CHECKING_ABSOLUTE_PATH.contains(expectedClass)) {
            // the class comes from jars
            TestUtils.equalsIgnoreWhiteSpaces(errorMessage, expectedTs,
                    actualContent);
            return;
        }
        removeAbsolutePathAndCompare(expectedClass, expectedTs, errorMessage,
                actualContent);
    }

    private void removeAbsolutePathAndCompare(Class<?> expectedClass,
            String expectedTs, String errorMessage, String actualContent) {
        Matcher matcher = JAVA_PATH_REFERENCE_REGEX.matcher(actualContent);

        Assert.assertTrue(errorMessage, matcher.find());

        String actualJavaFileReference = matcher.group(1);

        String actualContentWithoutPathReference = actualContent
                .replace(actualJavaFileReference, "");
        TestUtils.equalsIgnoreWhiteSpaces(errorMessage, expectedTs,
                actualContentWithoutPathReference);

        String javaFilePathReference = matcher.group(2);

        Class declaringClass = expectedClass;
        while (declaringClass.getDeclaringClass() != null) {
            declaringClass = declaringClass.getDeclaringClass();
        }
        String expectedEndingJavaFilePath = StringUtils.replaceChars(
                declaringClass.getCanonicalName(), '.', File.separatorChar)
                + ".java";
        String wrongPathMessage = String.format(
                "The generated model class '%s' refers to Java path '%s'. The path should end with '%s'",
                expectedClass, actualJavaFileReference,
                expectedEndingJavaFilePath);
        Assert.assertTrue(wrongPathMessage,
                javaFilePathReference.endsWith(expectedEndingJavaFilePath));
    }

    /**
     * Shorten the expected model file name. The format is:
     * <code>expected-model-lastPackageSegment.DeclaringClass(IfAny).ModelClass
     * .ts</code>. For example: The generated model of
     * <code>dev.hilla.SomeModel</code> will be expected to be the same as file
     * <code>expected-model-connect.SomeModel.ts</code>
     *
     * @param expectedClass
     * @return
     */
    private String constructExpectedModelFileName(Class<?> expectedClass) {
        String prefix = StringUtils
                .substringAfterLast(expectedClass.getPackage().getName(), ".");
        Class<?> declaringClass = expectedClass.getDeclaringClass();
        if (declaringClass != null) {
            prefix += "." + declaringClass.getSimpleName();
        }
        return String.format("expected-model-%s.%s.ts", prefix,
                expectedClass.getSimpleName());
    }
}
