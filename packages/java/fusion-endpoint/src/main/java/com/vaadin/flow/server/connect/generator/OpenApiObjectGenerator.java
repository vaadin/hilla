/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.generator;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.parametrization.ResolvedTypeParametersMap;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.SourceRoot.Callback;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.connect.Endpoint;
import com.vaadin.flow.server.connect.EndpointExposed;
import com.vaadin.flow.server.connect.EndpointNameChecker;

import static com.vaadin.flow.server.connect.ExplicitNullableTypeChecker.isRequired;

/**
 * Java parser class which scans for all {@link Endpoint} classes and produces
 * OpenApi json.
 */
public class OpenApiObjectGenerator {
    public static final String EXTENSION_VAADIN_CONNECT_PARAMETERS_DESCRIPTION = "x-vaadin-parameters-description";
    public static final String EXTENSION_VAADIN_FILE_PATH = "x-vaadin-file-path";
    public static final String CONSTRAINT_ANNOTATIONS = "x-annotations";
    private static final String EXTENSION_VAADIN_CONNECT_DEFERRABLE = "x-vaadin-connect-deferrable";
    private static final String VAADIN_CONNECT_OAUTH2_SECURITY_SCHEME = "vaadin-connect-oauth2";
    private static final String VAADIN_CONNECT_OAUTH2_TOKEN_URL = "/oauth/token";
    private final EndpointNameChecker endpointNameChecker = new EndpointNameChecker();
    private List<Path> javaSourcePaths = new ArrayList<>();
    private OpenApiConfiguration configuration;
    private Map<String, ResolvedReferenceType> usedTypes;
    private Map<ClassOrInterfaceDeclaration, String> endpointsJavadoc;
    private Map<String, TypeDeclaration<?>> nonEndpointMap;
    private Map<String, ClassOrInterfaceDeclaration> endpointExposedMap;
    private Map<String, String> qualifiedNameToPath;
    private Map<String, PathItem> pathItems;
    private Set<String> generatedSchema;
    private OpenAPI openApiModel;
    private ClassLoader typeResolverClassLoader;
    private SchemaGenerator schemaGenerator;
    private SchemaResolver schemaResolver;
    private boolean needsDeferrableImport = false;

    private static Logger getLogger() {
        return LoggerFactory.getLogger(OpenApiObjectGenerator.class);
    }

    /**
     * Adds the source path to the generator to process.
     *
     * @param sourcePath
     *            the source path to generate the metadata from
     */
    public void addSourcePath(Path sourcePath) {
        if (sourcePath == null) {
            throw new IllegalArgumentException(
                    "Java source path must be a valid directory");
        }
        if (!sourcePath.toFile().exists()) {
            throw new IllegalArgumentException(String
                    .format("Java source path '%s' doesn't exist", sourcePath));
        }
        this.javaSourcePaths.add(sourcePath);
    }

    /**
     * Set project's class loader which is used for resolving types from that
     * project.
     *
     * @param typeResolverClassLoader
     *            the project's class loader for type resolving
     */
    void setTypeResolverClassLoader(ClassLoader typeResolverClassLoader) {
        this.typeResolverClassLoader = typeResolverClassLoader;
    }

    /**
     * Sets the configuration to be used when generating an Open API spec.
     *
     * @param configuration
     *            the generator configuration
     */
    public void setOpenApiConfiguration(OpenApiConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the Open API, generates it if necessary.
     *
     * @return the Open API data
     */
    public OpenAPI getOpenApi() {
        if (openApiModel == null) {
            init();
        }
        return openApiModel;
    }

    OpenAPI generateOpenApi() {
        init();
        return openApiModel;
    }

    Schema parseResolvedTypeToSchema(ResolvedType resolvedType) {
        return schemaResolver.parseResolvedTypeToSchema(resolvedType);
    }

    Class<?> getClassFromReflection(ResolvedReferenceType resolvedType)
            throws ClassNotFoundException {
        String fullyQualifiedName = getFullyQualifiedName(resolvedType);
        if (typeResolverClassLoader != null) {
            return Class.forName(fullyQualifiedName, true,
                    typeResolverClassLoader);
        } else {
            return Class.forName(fullyQualifiedName);
        }
    }

    private void init() {
        if (javaSourcePaths == null || configuration == null) {
            throw new IllegalStateException(
                    "Java source path and configuration should not be null");
        }
        openApiModel = createBasicModel();
        nonEndpointMap = new HashMap<>();
        endpointExposedMap = new HashMap<>();
        qualifiedNameToPath = new HashMap<>();
        pathItems = new TreeMap<>();
        usedTypes = new HashMap<>();
        generatedSchema = new HashSet<>();
        endpointsJavadoc = new HashMap<>();
        schemaGenerator = new SchemaGenerator(this);
        schemaResolver = new SchemaResolver();
        needsDeferrableImport = false;
        ParserConfiguration parserConfiguration = createParserConfiguration();

        javaSourcePaths.stream()
                .map(path -> new SourceRoot(path, parserConfiguration))
                .forEach(sourceRoot -> parseSourceRoot(sourceRoot,
                        this::findEndpointExposed));

        javaSourcePaths.stream()
                .map(path -> new SourceRoot(path, parserConfiguration))
                .forEach(sourceRoot -> parseSourceRoot(sourceRoot,
                        this::process));

        for (Map.Entry<String, ResolvedReferenceType> entry : usedTypes
                .entrySet()) {
            List<Schema> schemas = createSchemasFromQualifiedNameAndType(
                    entry.getKey(), entry.getValue());
            schemas.forEach(schema -> {
                if (qualifiedNameToPath.get(schema.getName()) != null) {
                    schema.addExtension(EXTENSION_VAADIN_FILE_PATH,
                            qualifiedNameToPath.get(schema.getName()));
                }
                openApiModel.getComponents().addSchemas(schema.getName(),
                        schema);
            });
        }
        addTagsInformation();
    }

    private ParserConfiguration createParserConfiguration() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(false));
        if (typeResolverClassLoader != null) {
            combinedTypeSolver
                    .add(new ClassLoaderTypeSolver(typeResolverClassLoader));
        }
        return new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
    }

    private void parseSourceRoot(SourceRoot sourceRoot, Callback callback) {
        try {
            sourceRoot.parse("", callback);
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                    "Can't parse the java files in the source root '%s'",
                    sourceRoot), e);
        }
    }

    private void addTagsInformation() {
        for (Map.Entry<ClassOrInterfaceDeclaration, String> endpointJavadoc : endpointsJavadoc
                .entrySet()) {
            Tag tag = new Tag();
            ClassOrInterfaceDeclaration endpointDeclaration = endpointJavadoc
                    .getKey();
            String simpleClassName = endpointDeclaration.getNameAsString();
            tag.name(simpleClassName);
            tag.description(endpointJavadoc.getValue());
            tag.addExtension(EXTENSION_VAADIN_FILE_PATH,
                    qualifiedNameToPath.get(endpointDeclaration
                            .getFullyQualifiedName().orElse(simpleClassName)));
            openApiModel.addTagsItem(tag);
        }
    }

    private OpenAPI createBasicModel() {
        OpenAPI openAPI = new OpenAPI();

        Info info = new Info();
        info.setTitle(configuration.getApplicationTitle());
        info.setVersion(configuration.getApplicationApiVersion());
        openAPI.setInfo(info);

        Paths paths = new Paths();
        openAPI.setPaths(paths);

        Server server = new Server();
        server.setUrl(configuration.getServerUrl());
        server.setDescription(configuration.getServerDescription());
        openAPI.setServers(Collections.singletonList(server));
        Components components = new Components();
        SecurityScheme vaadinConnectOAuth2Scheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().password(new OAuthFlow()
                        .tokenUrl(VAADIN_CONNECT_OAUTH2_TOKEN_URL)
                        .scopes(new Scopes())));
        components.addSecuritySchemes(VAADIN_CONNECT_OAUTH2_SECURITY_SCHEME,
                vaadinConnectOAuth2Scheme);
        openAPI.components(components);
        return openAPI;
    }

    @SuppressWarnings("squid:S1172")
    private SourceRoot.Callback.Result process(Path localPath,
            Path absolutePath, ParseResult<CompilationUnit> result) {
        result.ifSuccessful(compilationUnit -> compilationUnit.getPrimaryType()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .filter(classOrInterfaceDeclaration -> !classOrInterfaceDeclaration
                        .isInterface())
                .filter(declaration -> !GeneratorUtils.hasAnnotation(
                        declaration, compilationUnit, EndpointExposed.class))
                .map(this::appendNestedClasses).orElse(Collections.emptyList())
                .forEach(classOrInterfaceDeclaration -> this.parseClass(
                        classOrInterfaceDeclaration, compilationUnit)));
        pathItems.forEach((pathName, pathItem) -> openApiModel.getPaths()
                .addPathItem(pathName, pathItem));
        if (needsDeferrableImport) {
            openApiModel.addExtension(EXTENSION_VAADIN_CONNECT_DEFERRABLE,
                    true);
        }
        return SourceRoot.Callback.Result.DONT_SAVE;
    }

    @SuppressWarnings("squid:S1172")
    private SourceRoot.Callback.Result findEndpointExposed(Path localPath,
            Path absolutePath, ParseResult<CompilationUnit> result) {
        result.ifSuccessful(compilationUnit -> compilationUnit.getPrimaryType()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .filter(declaration -> GeneratorUtils.hasAnnotation(declaration,
                        compilationUnit, EndpointExposed.class))
                .map(declaration -> endpointExposedMap.put(
                        declaration.resolve().getQualifiedName(),
                        declaration)));
        return SourceRoot.Callback.Result.DONT_SAVE;
    }

    private Collection<TypeDeclaration<?>> appendNestedClasses(
            ClassOrInterfaceDeclaration topLevelClass) {
        Set<TypeDeclaration<?>> nestedClasses = topLevelClass.getMembers()
                .stream()
                .filter(bodyDeclaration -> bodyDeclaration
                        .isClassOrInterfaceDeclaration()
                        || bodyDeclaration.isEnumDeclaration())
                .map(bodyDeclaration -> (TypeDeclaration<?>) bodyDeclaration
                        .asTypeDeclaration())
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator
                        .comparing(NodeWithSimpleName::getNameAsString))));
        nestedClasses.add(topLevelClass);
        return nestedClasses;
    }

    private void parseClass(TypeDeclaration<?> typeDeclaration,
            CompilationUnit compilationUnit) {
        if (typeDeclaration.isClassOrInterfaceDeclaration()) {
            parseClass(typeDeclaration.asClassOrInterfaceDeclaration(),
                    compilationUnit);
        } else if (typeDeclaration.isEnumDeclaration()) {
            EnumDeclaration enumDeclaration = typeDeclaration
                    .asEnumDeclaration();
            compilationUnit.getStorage().ifPresent(storage -> {
                String className = enumDeclaration.getFullyQualifiedName()
                        .orElse(enumDeclaration.getNameAsString());
                qualifiedNameToPath.put(className,
                        storage.getPath().toString());
            });
            nonEndpointMap.put(enumDeclaration.resolve().getQualifiedName(),
                    enumDeclaration);
        }
    }

    private void parseClass(ClassOrInterfaceDeclaration classDeclaration,
            CompilationUnit compilationUnit) {
        Optional<AnnotationExpr> endpointAnnotation = classDeclaration
                .getAnnotationByClass(Endpoint.class);
        compilationUnit.getStorage().ifPresent(storage -> {
            String className = classDeclaration.getFullyQualifiedName()
                    .orElse(classDeclaration.getNameAsString());
            qualifiedNameToPath.put(className, storage.getPath().toString());
        });
        if (!GeneratorUtils.hasAnnotation(classDeclaration, compilationUnit,
                Endpoint.class)) {
            nonEndpointMap.put(classDeclaration.resolve().getQualifiedName(),
                    classDeclaration);
        } else {
            Optional<Javadoc> javadoc = classDeclaration.getJavadoc();
            if (javadoc.isPresent()) {
                endpointsJavadoc.put(classDeclaration,
                        javadoc.get().getDescription().toText());
            } else {
                endpointsJavadoc.put(classDeclaration, "");
            }
            pathItems.putAll(createPathItems(
                    getEndpointName(classDeclaration,
                            endpointAnnotation.orElse(null)),
                    classDeclaration.getNameAsString(), classDeclaration,
                    ResolvedTypeParametersMap.empty(), compilationUnit));
        }
    }

    private String getEndpointName(ClassOrInterfaceDeclaration classDeclaration,
            AnnotationExpr endpointAnnotation) {
        String endpointName = Optional.ofNullable(endpointAnnotation)
                .filter(Expression::isSingleMemberAnnotationExpr)
                .map(Expression::asSingleMemberAnnotationExpr)
                .map(SingleMemberAnnotationExpr::getMemberValue)
                .map(Expression::asStringLiteralExpr)
                .map(LiteralStringValueExpr::getValue)
                .filter(GeneratorUtils::isNotBlank)
                .orElse(classDeclaration.getNameAsString());

        // detect the endpoint value name
        if (endpointName.equals(classDeclaration.getNameAsString())
                && endpointAnnotation != null) {
            String endpointValueName = getParameterValueFromAnnotation(
                    endpointAnnotation, "value");
            if (endpointValueName != null) {
                endpointName = endpointValueName.substring(1,
                        endpointValueName.length() - 1);
            }
        }

        String validationError = endpointNameChecker.check(endpointName);
        if (validationError != null) {
            throw new IllegalStateException(
                    String.format("Endpoint name '%s' is invalid, reason: '%s'",
                            endpointName, validationError));
        }
        return endpointName;
    }

    private String getParameterValueFromAnnotation(
            AnnotationExpr endpointAnnotation, String paramName) {
        return endpointAnnotation.getChildNodes().stream().filter(
                node -> node.getTokenRange().isPresent() && paramName.equals(
                        node.getTokenRange().get().getBegin().getText()))
                .map(node -> node.getTokenRange().get().getEnd().getText())
                .findFirst().orElse(null);
    }

    private List<Schema> parseNonEndpointClassAsSchema(
            String fullQualifiedName) {
        TypeDeclaration<?> typeDeclaration = nonEndpointMap
                .get(fullQualifiedName);
        if (typeDeclaration == null || typeDeclaration.isEnumDeclaration()) {
            return Collections.emptyList();
        }
        List<Schema> result = new ArrayList<>();

        Schema schema = schemaGenerator.createSingleSchema(fullQualifiedName,
                typeDeclaration);
        generatedSchema.add(fullQualifiedName);

        NodeList<ClassOrInterfaceType> extendedTypes = null;
        if (typeDeclaration.isClassOrInterfaceDeclaration()) {
            extendedTypes = typeDeclaration.asClassOrInterfaceDeclaration()
                    .getExtendedTypes();
        }
        if (extendedTypes == null || extendedTypes.isEmpty()) {
            result.add(schema);
            result.addAll(generatedRelatedSchemas(schema));
        } else {
            ComposedSchema parentSchema = new ComposedSchema();
            parentSchema.setName(fullQualifiedName);
            result.add(parentSchema);
            extendedTypes.forEach(parentType -> {
                ResolvedReferenceType resolvedParentType = parentType.resolve();
                String parentQualifiedName = resolvedParentType
                        .getQualifiedName();
                String parentRef = schemaResolver
                        .getFullQualifiedNameRef(parentQualifiedName);
                parentSchema.addAllOfItem(new ObjectSchema().$ref(parentRef));
                schemaResolver.addFoundTypes(parentQualifiedName,
                        resolvedParentType);
            });
            // The inserting order matters for `allof` property.
            parentSchema.addAllOfItem(schema);
            result.addAll(generatedRelatedSchemas(parentSchema));
        }
        return result;
    }

    private List<Schema> createSchemasFromQualifiedNameAndType(
            String qualifiedName, ResolvedReferenceType resolvedReferenceType) {
        List<Schema> list = parseNonEndpointClassAsSchema(qualifiedName);
        if (list.isEmpty()) {
            return parseReferencedTypeAsSchema(resolvedReferenceType);
        } else {
            return list;
        }
    }

    private Map<String, ResolvedReferenceType> collectUsedTypesFromSchema(
            Schema schema) {
        Map<String, ResolvedReferenceType> map = new HashMap<>();
        if (GeneratorUtils.isNotBlank(schema.getName())
                || GeneratorUtils.isNotBlank(schema.get$ref())) {
            String name = GeneratorUtils.firstNonBlank(schema.getName(),
                    schemaResolver.getSimpleRef(schema.get$ref()));
            ResolvedReferenceType resolvedReferenceType = schemaResolver
                    .getFoundTypeByQualifiedName(name);
            if (resolvedReferenceType != null) {
                map.put(name, resolvedReferenceType);
            } else {
                getLogger().info(
                        "Can't find the type information of class '{}'. "
                                + "This might result in a missing schema in the generated OpenAPI spec.",
                        name);
            }
        }
        if (schema instanceof ArraySchema) {
            map.putAll(collectUsedTypesFromSchema(
                    ((ArraySchema) schema).getItems()));
        } else if (schema instanceof MapSchema
                && schema.getAdditionalProperties() != null) {
            map.putAll(collectUsedTypesFromSchema(
                    (Schema) schema.getAdditionalProperties()));
        } else if (schema instanceof ComposedSchema
                && ((ComposedSchema) schema).getAllOf() != null) {
            for (Schema child : ((ComposedSchema) schema).getAllOf()) {
                map.putAll(collectUsedTypesFromSchema(child));
            }
        }
        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(
                    o -> map.putAll(collectUsedTypesFromSchema((Schema) o)));
        }
        return map;
    }

    private boolean isReservedWord(String word) {
        return word != null && EndpointNameChecker.ECMA_SCRIPT_RESERVED_WORDS
                .contains(word.toLowerCase());
    }

    private Pair<ClassOrInterfaceDeclaration, ResolvedTypeParametersMap> getDeclarationAndResolvedTypeParametersMap(
            ClassOrInterfaceType type,
            ResolvedTypeParametersMap parentResolvedTypeParametersMap) {
        ResolvedReferenceType resolvedType = parentResolvedTypeParametersMap
                .replaceAll(type.resolve()).asReferenceType();
        String qualifiedName = resolvedType.getQualifiedName();
        ClassOrInterfaceDeclaration declaration = endpointExposedMap
                .get(qualifiedName);
        if (declaration == null) {
            return null;
        }

        return new Pair<>(declaration, resolvedType.typeParametersMap());
    }

    private Map<String, PathItem> createPathItems(String endpointName,
            String tagName, ClassOrInterfaceDeclaration typeDeclaration,
            ResolvedTypeParametersMap resolvedTypeParametersMap,
            CompilationUnit compilationUnit) {
        Map<String, PathItem> newPathItems = new HashMap<>();
        Collection<MethodDeclaration> methods = typeDeclaration.getMethods();
        for (MethodDeclaration methodDeclaration : methods) {
            if (isAccessForbidden(typeDeclaration, methodDeclaration)) {
                continue;
            }
            String methodName = methodDeclaration.getNameAsString();

            Operation post = createPostOperation(methodDeclaration);
            if (methodDeclaration.getParameters().isNonEmpty()) {
                post.setRequestBody(createRequestBody(methodDeclaration,
                        resolvedTypeParametersMap));
            }

            ApiResponses responses = createApiResponses(methodDeclaration,
                    resolvedTypeParametersMap);
            post.setResponses(responses);
            post.tags(Collections.singletonList(tagName));
            PathItem pathItem = new PathItem().post(post);

            String pathName = "/" + endpointName + "/" + methodName;
            pathItem.readOperationsMap()
                    .forEach((httpMethod, operation) -> operation
                            .setOperationId(String.join("_", endpointName,
                                    methodName, httpMethod.name())));
            newPathItems.put(pathName, pathItem);
        }

        Stream.concat(typeDeclaration.getExtendedTypes().stream(),
                typeDeclaration.getImplementedTypes().stream())
                .map(resolvedType -> getDeclarationAndResolvedTypeParametersMap(
                        resolvedType, resolvedTypeParametersMap))
                .filter(Objects::nonNull)
                .forEach(pair -> newPathItems
                        .putAll(createPathItems(endpointName, tagName, pair.a,
                                pair.b, compilationUnit)));
        return newPathItems;
    }

    private boolean isAccessForbidden(
            ClassOrInterfaceDeclaration typeDeclaration,
            MethodDeclaration methodDeclaration) {
        return (typeDeclaration.isInterface() ? !methodDeclaration.isDefault()
                : !methodDeclaration.isPublic())
                || (hasSecurityAnnotation(methodDeclaration)
                        ? methodDeclaration.isAnnotationPresent(DenyAll.class)
                        : typeDeclaration.isAnnotationPresent(DenyAll.class));
    }

    private boolean hasSecurityAnnotation(MethodDeclaration method) {
        return method.isAnnotationPresent(AnonymousAllowed.class)
                || method.isAnnotationPresent(PermitAll.class)
                || method.isAnnotationPresent(DenyAll.class)
                || method.isAnnotationPresent(RolesAllowed.class);
    }

    private Operation createPostOperation(MethodDeclaration methodDeclaration) {
        Operation post = new Operation();
        SecurityRequirement securityItem = new SecurityRequirement();
        securityItem.addList(VAADIN_CONNECT_OAUTH2_SECURITY_SCHEME);
        post.addSecurityItem(securityItem);

        methodDeclaration.getJavadoc().ifPresent(javadoc -> post
                .setDescription(javadoc.getDescription().toText()));
        return post;
    }

    private ApiResponses createApiResponses(MethodDeclaration methodDeclaration,
            ResolvedTypeParametersMap resolvedTypeParametersMap) {
        ApiResponse successfulResponse = createApiSuccessfulResponse(
                methodDeclaration, resolvedTypeParametersMap);
        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", successfulResponse);
        return responses;
    }

    private ApiResponse createApiSuccessfulResponse(
            MethodDeclaration methodDeclaration,
            ResolvedTypeParametersMap resolvedTypeParametersMap) {
        Content successfulContent = new Content();
        // "description" is a REQUIRED property of Response
        ApiResponse successfulResponse = new ApiResponse().description("");
        methodDeclaration.getJavadoc().ifPresent(javadoc -> {
            for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                if (blockTag.getType() == JavadocBlockTag.Type.RETURN) {
                    successfulResponse.setDescription(
                            "Return " + blockTag.getContent().toText());
                }
            }
        });
        if (!methodDeclaration.getType().isVoidType()) {
            MediaType mediaItem = createReturnMediaType(methodDeclaration,
                    resolvedTypeParametersMap);
            successfulContent.addMediaType("application/json", mediaItem);
            successfulResponse.content(successfulContent);
        }
        return successfulResponse;
    }

    private MediaType createReturnMediaType(MethodDeclaration methodDeclaration,
            ResolvedTypeParametersMap resolvedTypeParametersMap) {
        MediaType mediaItem = new MediaType();
        ResolvedType resolvedType = resolvedTypeParametersMap
                .replaceAll(methodDeclaration.resolve().getReturnType());
        Schema schema = parseResolvedTypeToSchema(resolvedType);
        schema.setDescription("");
        if (GeneratorUtils.isTrue(schema.getNullable())
                && isRequired(methodDeclaration)) {
            schema.setNullable(null);
        }
        usedTypes.putAll(collectUsedTypesFromSchema(schema));
        mediaItem.schema(schema);
        return mediaItem;
    }

    private RequestBody createRequestBody(MethodDeclaration methodDeclaration,
            ResolvedTypeParametersMap resolvedTypeParametersMap) {
        Map<String, String> paramsDescription = new HashMap<>();
        methodDeclaration.getJavadoc().ifPresent(javadoc -> {
            for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                if (blockTag.getType() == JavadocBlockTag.Type.PARAM) {
                    paramsDescription.put(blockTag.getName().orElse(""),
                            blockTag.getContent().toText());
                }
            }
        });

        RequestBody requestBody = new RequestBody();
        Content requestBodyContent = new Content();
        requestBody.content(requestBodyContent);
        MediaType requestBodyObject = new MediaType();
        requestBodyContent.addMediaType("application/json", requestBodyObject);
        Schema requestSchema = new ObjectSchema();
        requestSchema.setRequired(new ArrayList<>());
        requestBodyObject.schema(requestSchema);

        methodDeclaration.getParameters().forEach(parameter -> {
            ResolvedType resolvedType = resolvedTypeParametersMap
                    .replaceAll(parameter.resolve().getType());
            Schema paramSchema = parseResolvedTypeToSchema(resolvedType);
            paramSchema.setDescription("");
            usedTypes.putAll(collectUsedTypesFromSchema(paramSchema));
            String name = (isReservedWord(parameter.getNameAsString()) ? "_"
                    : "").concat(parameter.getNameAsString());
            if (GeneratorUtils.isBlank(paramSchema.get$ref())) {
                paramSchema.description(
                        paramsDescription.remove(parameter.getNameAsString()));
            }
            requestSchema.addProperties(name, paramSchema);
            if (GeneratorUtils.isNotTrue(paramSchema.getNullable())
                    || isRequired(parameter)) {
                requestSchema.addRequiredItem(name);
            }
            paramSchema.setNullable(null);
        });
        if (!paramsDescription.isEmpty()) {
            requestSchema.addExtension(
                    EXTENSION_VAADIN_CONNECT_PARAMETERS_DESCRIPTION,
                    new LinkedHashMap<>(paramsDescription));
        }
        return requestBody;
    }

    @SuppressWarnings("squid:S1872")
    private List<Schema> parseReferencedTypeAsSchema(
            ResolvedReferenceType resolvedType) {
        List<Schema> results = new ArrayList<>();

        Schema schema = schemaGenerator
                .createSingleSchemaFromResolvedType(resolvedType);
        String qualifiedName = resolvedType.getQualifiedName();
        generatedSchema.add(qualifiedName);

        List<ResolvedReferenceType> directAncestors = resolvedType
                .getDirectAncestors().stream()
                .filter(parent -> parent.getTypeDeclaration().isClass()
                        && !Object.class.getName()
                                .equals(parent.getQualifiedName()))
                .collect(Collectors.toList());

        if (directAncestors.isEmpty()
                || resolvedType.getTypeDeclaration().isEnum()) {
            results.add(schema);
            results.addAll(generatedRelatedSchemas(schema));
        } else {
            ComposedSchema parentSchema = new ComposedSchema();
            parentSchema.name(qualifiedName);
            results.add(parentSchema);
            for (ResolvedReferenceType directAncestor : directAncestors) {
                String ancestorQualifiedName = directAncestor
                        .getQualifiedName();
                String parentRef = schemaResolver
                        .getFullQualifiedNameRef(ancestorQualifiedName);
                parentSchema.addAllOfItem(new ObjectSchema().$ref(parentRef));
                schemaResolver.addFoundTypes(ancestorQualifiedName,
                        directAncestor);
            }
            parentSchema.addAllOfItem(schema);
            results.addAll(generatedRelatedSchemas(parentSchema));
        }
        return results;
    }

    private List<Schema> generatedRelatedSchemas(Schema schema) {
        List<Schema> result = new ArrayList<>();
        collectUsedTypesFromSchema(schema).entrySet().stream()
                .filter(s -> !generatedSchema.contains(s.getKey()))
                .forEach(s -> result.addAll(
                        createSchemasFromQualifiedNameAndType(s.getKey(),
                                s.getValue())));
        return result;
    }

    /**
     * This method return a fully qualified name from a resolved reference type
     * which is correct for nested declaration as well. The
     * {@link ResolvedReferenceType#getQualifiedName()} returns a canonical name
     * instead of a fully qualified name, which is not correct for nested
     * classes to be used in reflection. That's why this method is implemented.
     *
     * {@see Related discussion about FullyQualifiedName and CanonicalName:
     * https://github.com/javaparser/javaparser/issues/1480}
     *
     * @param resolvedReferenceType
     *            the type to get fully qualified name
     * @return fully qualified name
     */
    private String getFullyQualifiedName(
            ResolvedReferenceType resolvedReferenceType) {
        ResolvedReferenceTypeDeclaration typeDeclaration = resolvedReferenceType
                .getTypeDeclaration();
        String packageName = typeDeclaration.getPackageName();
        String canonicalName = typeDeclaration.getQualifiedName();
        if (GeneratorUtils.isBlank(packageName)) {
            return GeneratorUtils.replaceChars(canonicalName, '.', '$');
        } else {
            String name = GeneratorUtils.substringAfterLast(canonicalName,
                    packageName + ".");
            return String.format("%s.%s", packageName,
                    GeneratorUtils.replaceChars(name, '.', '$'));
        }
    }
}
