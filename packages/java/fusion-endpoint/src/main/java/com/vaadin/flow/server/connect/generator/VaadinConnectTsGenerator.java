/*
 * Copyright 2000-2020 Vaadin Ltd.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenResponse;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.auth.AuthParser;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.typescript.AbstractTypeScriptClientCodegen;
import io.swagger.parser.OpenAPIParser;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.connect.EndpointNameChecker;

import static com.vaadin.flow.server.connect.generator.OpenApiObjectGenerator.CONSTRAINT_ANNOTATIONS;

/**
 * Vaadin fusion JavaScript generator implementation for swagger-codegen. Some
 * parts of the implementation are copied from
 * {@link io.swagger.codegen.languages.JavascriptClientCodegen}
 */
public class VaadinConnectTsGenerator extends AbstractTypeScriptClientCodegen {

    public static final String TS = ".ts";
    public static final String MODEL = "Model";
    public static final String MODEL_TS = MODEL + TS;
    public static final String OPTIONAL_SUFFIX = " | undefined";
    private static final String GENERATOR_NAME = "javascript-vaadin-connect";
    private static final String EXTENSION_VAADIN_CONNECT_PARAMETERS = "x-vaadin-connect-parameters";
    private static final String EXTENSION_VAADIN_CONNECT_SHOW_TSDOC = "x-vaadin-connect-show-tsdoc";
    private static final String EXTENSION_VAADIN_CONNECT_METHOD_NAME = "x-vaadin-connect-method-name";
    private static final String EXTENSION_VAADIN_CONNECT_SERVICE_NAME = "x-vaadin-connect-endpoint-name";
    private static final String VAADIN_CONNECT_CLASS_DESCRIPTION = "vaadinConnectClassDescription";
    private static final String VAADIN_FILE_PATH = "vaadinFilePath";
    private static final String CLIENT_PATH_TEMPLATE_PROPERTY = "vaadinConnectDefaultClientPath";
    private static final Pattern PATH_REGEX = Pattern
            .compile("^/([^/{}\n\t]+)/([^/{}\n\t]+)$");
    private static final String JAVA_NAME_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    // Pattern for matching fully qualified name in a complex type
    // e.g. 'com.example.mypackage.Bean' will be extracted in the type
    // `Map<String, Map<String, com.example.mypackage.Bean>>`
    private static final Pattern FULLY_QUALIFIED_NAME_PATTERN = Pattern.compile(
            "(" + JAVA_NAME_PATTERN + "(\\." + JAVA_NAME_PATTERN + ")*)");
    private static final Pattern ARRAY_TYPE_NAME_PATTERN = Pattern
            .compile("Array<(.*)>");
    private static final Pattern MAPPED_TYPE_NAME_PATTERN = Pattern.compile(
            "\\{ \\[key: string\\]: (.*); \\}");
    private static final Pattern PRIMITIVE_TYPE_NAME_PATTERN =
            Pattern.compile("^(string|number|boolean)");
    private static final String OPERATION = "operation";
    private static final String IMPORT = "import";

    private List<Tag> tags;

    private static class VaadinConnectTSOnlyGenerator extends DefaultGenerator {
        @Override
        public File writeToFile(String filename, String contents)
                throws IOException {
            if (filename.endsWith(TS)) {
                return super.writeToFile(filename, contents);
            }
            return null;
        }
    }

    /**
     * Create vaadin ts codegen instance.
     */
    public VaadinConnectTsGenerator() {
        super();

        // set the output folder here
        outputFolder = "target/generated-resources/ts";

        /*
         * Api classes. You can write classes for each Api file with the
         * apiTemplateFiles map. as with models, add multiple entries with
         * different extensions for multiple files per class
         */
        apiTemplateFiles.put("TypeScriptApiTemplate.mustache", TS);
        modelTemplateFiles.put("EntityTemplate.mustache", TS);
        modelTemplateFiles.put("EntityModelTemplate.mustache", MODEL_TS);

        /*
         * Template Location. This is the location which templates will be read
         * from. The generator will use the resource stream to attempt to read
         * the templates.
         */
        templateDir = "com/vaadin/flow/server/connect/generator";

        /*
         * Reserved words copied from
         * https://www.w3schools.com/js/js_reserved.asp
         */
        reservedWords
                .addAll(EndpointNameChecker.ECMA_SCRIPT_RESERVED_WORDS);
        reservedWords.addAll(languageSpecificPrimitives);
        typeMapping.put("BigDecimal", "number");
        typeMapping.put("map", "Map");
        typeMapping.put("Map", "Map");
        typeMapping.put("DateTime", "string");
        typeMapping.put("Date", "string");
    }

    /**
     * Runs the code generation based on the data from the OpenAPI json.
     * Generates the target files in the directory specified, overwriting the
     * files and creating the target directory, if necessary.
     *
     * @param openApiJsonFile
     *            the api spec file to analyze
     * @param generatedFrontendDirectory
     *            the directory to generateOpenApiSpec the files into
     *
     * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
     *      specification</a>
     * @return true if any file was generated.
     */
    public static boolean launch(File openApiJsonFile,
            File generatedFrontendDirectory) {
        return launch(openApiJsonFile, generatedFrontendDirectory, null);
    }

    /**
     * Runs the code generation based on the data from the OpenAPI json.
     * Generates the target files in the directory specified, overwriting the
     * files and creating the target directory, if necessary.
     *
     * @param openApiJsonFile
     *            the api spec file to analyze
     * @param generatedFrontendDirectory
     *            the directory to generateOpenApiSpec the files into
     * @param defaultClientPath
     *            the default client path which is imported in the generated
     *            files. If it is {@code null}, the default generate client path
     *            is used.
     * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
     *      specification</a>
     * @return true if any file was generated.
     */
    public static boolean launch(File openApiJsonFile,
            File generatedFrontendDirectory, String defaultClientPath) {

        // There is no openApi file, thus remove old stuff.
        if (!openApiJsonFile.exists()) {
            FileUtils.deleteQuietly(generatedFrontendDirectory);
            return false;
        }

        CodegenConfigurator configurator = new CodegenConfigurator();
        configurator.setLang(VaadinConnectTsGenerator.class.getName());
        configurator.setInputSpecURL(openApiJsonFile.toString());
        configurator.setOutputDir(generatedFrontendDirectory.toString());
        configurator.addAdditionalProperty(CLIENT_PATH_TEMPLATE_PROPERTY,
                getDefaultClientPath(defaultClientPath));

        return !generate(configurator).isEmpty();
    }

    private static String removeTsExtension(String path) {
        if (path.endsWith(TS)) {
            return path.substring(0, path.length() - 3);
        }
        return path;
    }

    private static String getDefaultClientPath(String path) {
        path = GeneratorUtils.defaultIfNull(path,
                VaadinConnectClientGenerator.CONNECT_CLIENT_IMPORT_PATH);
        return removeTsExtension(path);
    }

    private static Set<File> generate(CodegenConfigurator configurator) {
        SwaggerParseResult parseResult = getParseResult(configurator);
        if (parseResult != null && parseResult.getMessages().isEmpty()) {
            OpenAPI openAPI = parseResult.getOpenAPI();
            if (openAPI.getComponents() == null) {
                openAPI.setComponents(new Components());
            }
            ClientOptInput clientOptInput = configurator.toClientOptInput()
                    .openAPI(openAPI);
            Set<File> generatedFiles = new VaadinConnectTSOnlyGenerator()
                    .opts(clientOptInput).generate().stream()
                    .filter(Objects::nonNull).collect(Collectors.toSet());

            cleanGeneratedFolder(configurator.getOutputDir(), generatedFiles);

            return generatedFiles.stream()
                    .filter(file -> file.getName().endsWith(TS))
                    .collect(Collectors.toSet());
        } else {
            String error = parseResult == null ? ""
                    : String.join("", parseResult.getMessages());
            cleanGeneratedFolder(configurator.getOutputDir(),
                    Collections.emptySet());
            throw getUnexpectedOpenAPIException(configurator.getInputSpecURL(),
                    error);
        }

    }

    private static void cleanGeneratedFolder(String outputDir,
            Set<File> generatedFiles) {
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            return;
        }
        deleteStaleFiles(generatedFiles, outputDirFile);
        deleteEmptyFolders(outputDirFile);
    }

    private static void deleteEmptyFolders(File outputDirFile) {
        Collection<File> emptyFolders = getEmptyFolders(outputDirFile);
        for (File file : emptyFolders) {
            getLogger().info("Removing empty folder '{}'.",
                    file.getAbsolutePath());
            deleteFile(file);
        }
    }

    private static void deleteStaleFiles(Set<File> generatedFiles,
            File outputDirFile) {
        Collection<File> filesToDelete = getFilesToDelete(generatedFiles,
                outputDirFile);
        for (File file : filesToDelete) {
            getLogger().info("Removing stale generated file '{}'.",
                    file.getAbsolutePath());
            deleteFile(file);
        }
    }

    private static Collection<File> getFilesToDelete(Set<File> generatedFiles,
            File outputDirFile) {
        return FileUtils.listFiles(outputDirFile, new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return shouldDelete(generatedFiles, file);
            }
        }, TrueFileFilter.INSTANCE);
    }

    private static Collection<File> getEmptyFolders(File file) {
        if (file == null || !file.isDirectory()) {
            return Collections.emptyList();
        }
        Set<File> emptyFolders = new HashSet<>();
        File[] children = file.listFiles();
        if (children == null || children.length == 0) {
            emptyFolders.add(file);
        } else {
            for (File child : children) {
                emptyFolders.addAll(getEmptyFolders(child));
            }
        }
        return emptyFolders;
    }

    private static void deleteFile(File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            getLogger().info(String.format(
                    "Failed to remove '%s' while cleaning the generated folder.",
                    file.getAbsolutePath()), e);
        }
    }

    private static boolean shouldDelete(Set<File> generatedFiles, File file) {
        return !generatedFiles.contains(file)
                && !VaadinConnectClientGenerator.CONNECT_CLIENT_NAME
                        .equals(file.getName());
    }

    private static IllegalStateException getUnexpectedOpenAPIException(
            String inputFile, String errorMessage) {
        return new IllegalStateException(
                "Unexpected error while generating Vaadin TypeScript endpoint wrappers."
                        + " The input file " + inputFile
                        + " might be corrupted, please try running the generating tasks again. "
                        + errorMessage);
    }

    private static SwaggerParseResult getParseResult(
            CodegenConfigurator configurator) {
        try {
            List<AuthorizationValue> authorizationValues = AuthParser
                    .parse(configurator.getAuth());
            String inputSpec = configurator.loadSpecContent(
                    configurator.getInputSpecURL(), authorizationValues);
            ParseOptions options = new ParseOptions();
            options.setResolve(true);
            return new OpenAPIParser().readContents(inputSpec,
                    authorizationValues, options);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unexpected error while generating Vaadin TypeScript endpoint wrappers. "
                            + String.format("Can't read file '%s'",
                                    configurator.getInputSpecURL()),
                    e);
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VaadinConnectTsGenerator.class);
    }

    private static boolean isDebugConnectMavenPlugin() {
        return System.getProperty("debugConnectMavenPlugin") != null;
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see io.swagger.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    /**
     * Configures a friendly name for the generator. This will be used by the
     * generator to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return GENERATOR_NAME;
    }

    /**
     * Returns human-friendly help for the generator. Provide the consumer with
     * help tips, parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a Vaadin endpoint wrappers.";
    }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle
     * escaping those terms here. This logic is only called if a variable
     * matches the reserved words
     *
     * @return the escaped term
     */
    @Override
    public String escapeReservedWord(String name) {
        return this.reservedWordsMappings().getOrDefault(name, "_" + name);
    }

    /**
     * Location to write model files. You can use the modelPackage() as defined
     * when the class is instantiated
     */
    @Override
    public String modelFileFolder() {
        return outputFolder;
    }

    @Override
    public String toModelFilename(String name) {
        if (!GeneratorUtils.contains(name, ".")) {
            return super.toModelFilename(name);
        }
        String packageName = GeneratorUtils.substringBeforeLast(name, ".");
        packageName = packageName.replaceAll("\\.", "/");

        String modelName = GeneratorUtils.substringAfterLast(name, ".");
        modelName = super.toModelFilename(modelName);

        return packageName + "/" + modelName;
    }

    @Override
    public String toModelName(String name) {
        return name;
    }

    /**
     * Location to write api files. You can use the apiPackage() as defined when
     * the class is instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder;
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // Escape opening/closing block comment to avoid code injection
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove ', " to avoid code injection
        return input.replace("\"", "").replace("'", "");
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod,
            Operation operation, Map<String, Schema> schemas, OpenAPI openAPI) {
        if (!"POST".equalsIgnoreCase(httpMethod)) {
            throw getGeneratorException(
                    "Code generator only supports POST requests.");
        }
        Matcher matcher = PATH_REGEX.matcher(path);
        if (!matcher.matches()) {
            throw getGeneratorException(
                    "Path must be in form of \"/<EndpointName>/<MethodName>\".");
        }
        CodegenOperation codegenOperation = super.fromOperation(path,
                httpMethod, operation, schemas, openAPI);
        String endpointName = matcher.group(1);
        String methodName = matcher.group(2);
        codegenOperation.getVendorExtensions()
                .put(EXTENSION_VAADIN_CONNECT_METHOD_NAME, methodName);
        codegenOperation.getVendorExtensions()
                .put(EXTENSION_VAADIN_CONNECT_SERVICE_NAME, endpointName);
        validateOperationTags(path, httpMethod, operation);
        return codegenOperation;
    }

    @Override
    public String getSchemaType(Schema schema) {
        if (isNullableWrapperSchema(schema) && schema instanceof ComposedSchema) {
            Schema wrappedSchema = ((ComposedSchema) schema).getAllOf().get(0);
            return super.getSchemaType(wrappedSchema);
        }
        return super.getSchemaType(schema);
    }

    private boolean isNullableWrapperSchema(Schema schema) {
        if (!(schema instanceof ComposedSchema)) {
            return false;
        }
        boolean hasOnlyOneSchemaInAllOf = ((ComposedSchema) schema)
                .getAllOf() != null
                && ((ComposedSchema) schema).getAllOf().size() == 1;
        boolean hasNoOneOfAndAnyOf = isNullOrEmptyList(
                ((ComposedSchema) schema).getOneOf())
                && isNullOrEmptyList(((ComposedSchema) schema).getAnyOf());
        return hasOnlyOneSchemaInAllOf && hasNoOneOfAndAnyOf;
    }

    private boolean isNullOrEmptyList(List list) {
        return list == null || list.isEmpty();
    }

    private String getSimpleNameFromImports(String dataType,
            List<Map<String, String>> imports) {
        for (Map<String, String> anImport : imports) {
            if (GeneratorUtils.equals(dataType, anImport.get(IMPORT))) {
                return GeneratorUtils.firstNonBlank(anImport.get("importAs"),
                        anImport.get("className"));
            }
        }
        if (GeneratorUtils.contains(dataType, "<")
                || GeneratorUtils.contains(dataType, "{")
                || GeneratorUtils.contains(dataType, "|")) {
            return getSimpleNameFromComplexType(dataType, imports);
        }
        return getSimpleNameFromQualifiedName(dataType);
    }

    private String getModelArguments(CodegenProperty property,
            List<Map<String, String>> imports) {
        String dataType = property.datatype;
        boolean optional = !property.required;
        String simpleName = getSimpleNameFromImports(dataType, imports);
        return getModelVariableArguments(simpleName, optional,
                getConstrainsArguments(property));
    }

    private String getModelFullType(String name) {
        Matcher matcher = ARRAY_TYPE_NAME_PATTERN.matcher(name);
        if (matcher.find()) {
            String variableName = matcher.group(1);
            return "Array" + MODEL + "<" + getModelVariableType(variableName)
                    + ", " + getModelFullType(variableName) + ">";
        }
        matcher = MAPPED_TYPE_NAME_PATTERN.matcher(name);
        if (matcher.find()) {
            return "Object" + MODEL + "<{ [key: string]: "
                    + getModelVariableType(matcher.group(1)) + "; }>";
        }
        return fixNameForModel(name);
    }

    private String getModelVariableType(String variableName) {
        Matcher matcher = PRIMITIVE_TYPE_NAME_PATTERN.matcher(variableName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return MODEL + "Type<" + getModelFullType(variableName) + ">";
    }

    private String getModelVariableArguments(String name, boolean optional,
            List<String> constrainArguments) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fixNameForModel(name));
        stringBuilder.append(", [");
        List<String> arguments = new ArrayList<>();
        arguments.add(String.valueOf(optional));
        Matcher matcher = ARRAY_TYPE_NAME_PATTERN.matcher(name);
        if (matcher.find()) {
            String arrayTypeName = matcher.group(1);
            boolean arrayTypeOptional = arrayTypeName.endsWith(OPTIONAL_SUFFIX);
            arrayTypeName = removeOptionalSuffix(arrayTypeName);
            arguments.add(getModelVariableArguments(arrayTypeName, arrayTypeOptional, 
                Collections.emptyList()));
        }
        if (!constrainArguments.isEmpty()) {
            arguments.addAll(constrainArguments);
        }
        stringBuilder
                .append(arguments.stream().collect(Collectors.joining(", ")));
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private String removeOptionalSuffix(String name) {
        if (name.endsWith(OPTIONAL_SUFFIX)) {
            return name.substring(0, name.length() - OPTIONAL_SUFFIX.length());
        }
        return name;
    }

    private String fixNameForModel(String name) {
        name = removeOptionalSuffix(name);
        if (ARRAY_TYPE_NAME_PATTERN.matcher(name).find()) {
            name = "Array";
        } else if ("any".equals(name) || MAPPED_TYPE_NAME_PATTERN.matcher(name).find()) {
            name = "Object";
        } else if (PRIMITIVE_TYPE_NAME_PATTERN.matcher(name).find()) {
            name = GeneratorUtils.capitalize(name);
        }
        return name + MODEL;
    }

    private List<String> getConstrainsArguments(CodegenProperty property) {
        List<String> annotations = (List) property.getVendorExtensions()
                .get(CONSTRAINT_ANNOTATIONS);
        if (annotations != null) {
            return annotations.stream()
                    .map(annotation -> String.format("new " + "%s", annotation))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getSimpleNameFromComplexType(String dataType,
            List<Map<String, String>> imports) {
        Matcher matcher = FULLY_QUALIFIED_NAME_PATTERN.matcher(dataType);
        StringBuffer builder = new StringBuffer();
        while (matcher.find()) {
            String fqnName = matcher.group(1);
            matcher.appendReplacement(builder,
                    getSimpleNameFromImports(fqnName, imports));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private String getSimpleNameFromQualifiedName(String qualifiedName) {
        if (GeneratorUtils.contains(qualifiedName, ".")) {
            return GeneratorUtils.substringAfterLast(qualifiedName, ".");
        }
        return qualifiedName;
    }

    private String convertQualifiedNameToModelPath(String qualifiedName) {
        return "./" + GeneratorUtils.replaceChars(qualifiedName, '.', '/');
    }

    private void validateOperationTags(String path, String httpMethod,
            Operation operation) {
        List<String> operationTags = operation.getTags();
        if (operationTags == null || operationTags.isEmpty()) {
            getLogger().warn(
                    "The '{}' operation with path '{}' does not have any tag. The generated method will be included in 'Default' Endpoint.",
                    httpMethod, path);
        } else if (operationTags.size() > 1) {
            String fileList = String.join(", ", operationTags);
            getLogger().warn(
                    "The '{}' operation with path '{}' contains multiple tags. The generated method will be included in classes: '{}'.",
                    httpMethod, path, fileList);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs
                .get("operations");
        String classname = (String) operations.get("classname");
        for (Tag tag : tags) {
            if (tag.getName().equals(classname)) {
                objs.put(VAADIN_CONNECT_CLASS_DESCRIPTION,
                        tag.getDescription());
                setVaadinFilePath(objs, tag);
                break;
            }
        }
        if (objs.get(VAADIN_CONNECT_CLASS_DESCRIPTION) == null) {
            warnNoClassInformation(classname);
        }

        if ((operations.get(OPERATION) instanceof List)) {
            List<CodegenOperation> codegenOperations = (List<CodegenOperation>) operations
                    .get(OPERATION);
            setShouldShowTsDoc(codegenOperations);
        }
        Map<String, Object> postProcessOperations = super.postProcessOperations(
                objs);
        List<Map<String, Object>> imports = (List<Map<String, Object>>) objs
                .get("imports");
        adjustImportInformationForEndpoints(imports);

        printDebugMessage(postProcessOperations, "=== All operations data ===");
        return postProcessOperations;
    }

    private void setVaadinFilePath(Map<String, Object> objs, Tag tag) {
        if (tag.getExtensions() != null && tag.getExtensions().get(
                OpenApiObjectGenerator.EXTENSION_VAADIN_FILE_PATH) != null) {
            objs.put(VAADIN_FILE_PATH, tag.getExtensions()
                    .get(OpenApiObjectGenerator.EXTENSION_VAADIN_FILE_PATH));
        }
    }

    @Override
    protected void addImport(CodegenModel m, String type) {
        if (!GeneratorUtils.equals(m.getName(), type)) {
            super.addImport(m, type);
        }
    }

    @Override
    public Map<String, Object> postProcessAllModels(
            Map<String, Object> processedModels) {
        Map<String, Object> postProcessAllModels = super.postProcessAllModels(
                processedModels);
        for (Map.Entry<String, Object> modelEntry : postProcessAllModels
                .entrySet()) {
            Map<String, Object> model = (Map<String, Object>) modelEntry
                    .getValue();
            List<Map<String, Object>> imports = (List<Map<String, Object>>) model
                    .get("imports");
            adjustImportInformationForModel(imports,
                    (String) model.get("classname"));
        }

        printDebugMessage(processedModels, "=== All models data ===");

        return postProcessAllModels;
    }

    @Override
    public CodegenModel fromModel(String name, Schema schema,
            Map<String, Schema> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, schema,
                allDefinitions);
        Set<String> imports = collectImportsFromSchema(schema);
        imports.removeIf(type -> type.equals(name)); // Remove self imports
        codegenModel.setImports(imports);
        return codegenModel;
    }


    private void printDebugMessage(Object data, String message) {
        if (isDebugConnectMavenPlugin()) {
            getLogger().info(message);
            Json.prettyPrint(data);
        }
    }

    private void adjustImportInformationForEndpoints(
            List<Map<String, Object>> imports) {
        adjustImportInformation(imports, ".");
    }

    private void adjustImportInformationForModel(
            List<Map<String, Object>> imports,
            String qualifiedNameForRelative) {
        String modelFilePath = convertQualifiedNameToModelPath(
                qualifiedNameForRelative);
        // Remove the class name, only consider the parent folder
        modelFilePath = GeneratorUtils.substringBeforeLast(modelFilePath, "/");
        adjustImportInformation(imports, modelFilePath);
    }

    /**
     * Adjust the import paths.
     *
     * @param imports
     *            import paths list.
     * @param relativePathFromGeneratedFolderToCurrentFile
     *            relative path from the generated folder to the folder of the
     *            file where import paths will be written.
     */
    private void adjustImportInformation(List<Map<String, Object>> imports,
            String relativePathFromGeneratedFolderToCurrentFile) {
        Set<String> usedNames = new HashSet<>();
        // Make sure the import list are always in the same orders in when
        // generating different times.
        imports.sort((o1, o2) -> GeneratorUtils.compare((String) o1.get(IMPORT),
                (String) o2.get(IMPORT)));
        for (Map<String, Object> anImport : imports) {
            String importQualifiedName = (String) anImport.get(IMPORT);
            String className = getSimpleNameFromQualifiedName(
                    importQualifiedName);
            if (usedNames.contains(className)) {
                String importAs = getUniqueNameFromQualifiedName(usedNames,
                        importQualifiedName);
                anImport.put("importAs", importAs);
                usedNames.add(importAs);
            } else {
                usedNames.add(className);
            }
            anImport.put("className", className);

            String importPath = convertQualifiedNameToModelPath(
                    importQualifiedName);
            String relativizedPath = Paths
                    .get(relativePathFromGeneratedFolderToCurrentFile)
                    .relativize(Paths.get(importPath)).toString();
            relativizedPath = relativePathToNodeImport(relativizedPath);
            anImport.put("importPath", relativizedPath);
        }
    }

    private String relativePathToNodeImport(String relativePath) {
        // on Windows Node imports should still use Unix-style path separator
        relativePath = relativePath.replace("\\", "/");
        // prepend with `./` if the string does not start with `./`, `.` or `/`
        return relativePath.replaceFirst("^(?!(\\./|\\.|/))", "./");
    }

    private String getUniqueNameFromQualifiedName(Set<String> usedNames,
            String qualifiedName) {
        String[] packageSegments = qualifiedName == null ? null : qualifiedName.split("\\.");
        StringBuilder classNameBuilder = new StringBuilder();
        String newClassName = "";
        if (packageSegments != null && packageSegments.length > 1) {
            for (int i = packageSegments.length - 1; i >= 0; i--) {
                classNameBuilder.insert(0,
                        GeneratorUtils.capitalize(packageSegments[i]));
                newClassName = classNameBuilder.toString();
                if (!usedNames.contains(newClassName)) {
                    return newClassName;
                }
            }
        } else {
            newClassName = qualifiedName;
        }
        int counter = 1;
        while (usedNames.contains(newClassName)) {
            newClassName = qualifiedName + counter;
            counter++;
        }
        return newClassName;
    }

    private void setShouldShowTsDoc(List<CodegenOperation> operations) {
        for (CodegenOperation coop : operations) {
            boolean hasDescription = GeneratorUtils.isNotBlank(coop.getNotes());
            boolean hasParameter = hasParameterDescription(coop);
            boolean hasResponseDescription = hasResponseDescription(coop);
            if (hasDescription || hasParameter || hasResponseDescription) {
                coop.getVendorExtensions()
                        .put(EXTENSION_VAADIN_CONNECT_SHOW_TSDOC, true);
            }
        }
    }

    private boolean hasResponseDescription(CodegenOperation coop) {
        for (CodegenResponse response : coop.getResponses()) {
            if (GeneratorUtils.isNotBlank(response.getMessage())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasParameterDescription(CodegenOperation coop) {
        for (CodegenParameter bodyParam : coop.getBodyParams()) {
            List<ParameterInformation> parametersList = (List<ParameterInformation>) bodyParam
                    .getVendorExtensions()
                    .get(EXTENSION_VAADIN_CONNECT_PARAMETERS);
            if (parametersList != null && parametersList.stream()
                    .anyMatch(parameterInformation -> GeneratorUtils.isNotBlank(
                            parameterInformation.getDescription()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.processOpenAPI(openAPI);
        List<Tag> openAPITags = openAPI.getTags();
        this.tags = openAPITags != null ? openAPITags : Collections.emptyList();
    }

    private void warnNoClassInformation(String classname) {
        getLogger().info(
                "The class '{}' doesn't have JavaDoc or it is invalid. This results in no TsDoc for the generated module '{}'.",
                classname, classname);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CodegenParameter fromRequestBody(RequestBody body,
            Map<String, Schema> schemas, Set<String> imports) {
        CodegenParameter codegenParameter = super.fromRequestBody(body, schemas,
                imports);
        Schema requestBodySchema = getRequestBodySchema(body);
        if (requestBodySchema != null) {
            imports.addAll(collectImportsFromSchema(requestBodySchema));
            List<ParameterInformation> paramsList = getParamsList(
                    requestBodySchema);
            codegenParameter.getVendorExtensions()
                    .put(EXTENSION_VAADIN_CONNECT_PARAMETERS, paramsList);
        }
        return codegenParameter;
    }

    private Set<String> collectImportsFromSchema(Schema schema) {
        Set<String> imports = new HashSet<>();
        if (GeneratorUtils.isNotBlank(schema.get$ref())) {
            imports.add(getSimpleRef(schema.get$ref()));
        }
        if (schema instanceof ArraySchema) {
            imports.addAll(collectImportsFromSchema(
                    ((ArraySchema) schema).getItems()));
        } else if (schema instanceof MapSchema
                || schema.getAdditionalProperties() instanceof Schema) {
            imports.addAll(collectImportsFromSchema(
                    (Schema) schema.getAdditionalProperties()));
        } else if (schema instanceof ComposedSchema) {
            for (Schema child : ((ComposedSchema) schema).getAllOf()) {
                imports.addAll(collectImportsFromSchema(child));
            }
        }
        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(
                    o -> imports.addAll(collectImportsFromSchema((Schema) o)));
        }
        return imports;
    }

    private List<ParameterInformation> getParamsList(Schema requestSchema) {
        Map<String, Schema> properties = requestSchema.getProperties();
        List<ParameterInformation> paramsList = new ArrayList<>();
        List<String> requiredParams = requestSchema.getRequired() != null
                ? requestSchema.getRequired()
                : Collections.emptyList();
        for (Map.Entry<String, Schema> entry : properties.entrySet()) {
            String name = entry.getKey();
            boolean isRequired = requiredParams.contains(name);
            name = isReservedWord(name) ? escapeReservedWord(name) : name;
            String type = getTypeDeclaration(entry.getValue());
            String description = entry.getValue().getDescription();
            if (GeneratorUtils.isBlank(description)) {
                description = getDescriptionFromParameterExtension(name,
                        requestSchema);
            }
            ParameterInformation parameterInformation = new ParameterInformation(
                    name, isRequired, type, description);
            paramsList.add(parameterInformation);
        }
        return paramsList;
    }

    private String getDescriptionFromParameterExtension(String paramName,
            Schema requestSchema) {
        if (requestSchema.getExtensions() == null) {
            return "";
        }
        Map<String, String> paramDescription = (Map<String, String>) requestSchema
                .getExtensions()
                .get(OpenApiObjectGenerator.EXTENSION_VAADIN_CONNECT_PARAMETERS_DESCRIPTION);
        return paramDescription.getOrDefault(paramName, "");
    }

    @Override
    public String getTypeDeclaration(Schema schema) {
        String optionalSuffix = "";
        if (GeneratorUtils.isTrue(schema.getNullable())) {
            optionalSuffix = OPTIONAL_SUFFIX;
        }
        if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            Schema inner = arraySchema.getItems();
            return String.format("Array<%s>%s", this.getTypeDeclaration(inner),
                    optionalSuffix);
        } else if (GeneratorUtils.isNotBlank(schema.get$ref())) {
            return getSimpleRef(schema.get$ref()) + optionalSuffix;
        } else if (schema.getAdditionalProperties() != null) {
            Schema inner = (Schema) schema.getAdditionalProperties();
            return String.format("{ [key: string]: %s; }%s",
                    getTypeDeclaration(inner), optionalSuffix);
        } else if (schema instanceof ComposedSchema) {
            return getTypeDeclarationFromComposedSchema((ComposedSchema) schema,
                    optionalSuffix);
        } else {
            return super.getTypeDeclaration(schema) + optionalSuffix;
        }
    }

    private String getTypeDeclarationFromComposedSchema(
            ComposedSchema composedSchema, String optionalSuffix) {
        if (composedSchema.getAllOf() != null
                && composedSchema.getAllOf().size() == 1) {
            return getTypeDeclaration(composedSchema.getAllOf().get(0))
                    + optionalSuffix;
        } else {
            String unknownComposedSchema = Json.pretty(composedSchema);
            getLogger().debug("Unknown ComposedSchema: {}",
                    unknownComposedSchema);
            return "any";
        }
    }

    private Schema getRequestBodySchema(RequestBody body) {
        Content content = body.getContent();
        if (content == null) {
            return null;
        }
        MediaType mediaType = content.get(DEFAULT_CONTENT_TYPE);
        if (mediaType != null && mediaType.getSchema() != null) {
            return mediaType.getSchema();
        }
        return null;
    }

    @Override
    public String toApiName(String name) {
        return initialCaps(name);
    }

    @Override
    protected String getTemplateDir() {
        return templateDir;
    }

    @Override
    public String getDefaultTemplateDir() {
        return templateDir;
    }

    private RuntimeException getGeneratorException(String message) {
        return new RuntimeException(message
                + " For more information, please checkout the Vaadin TypeScript Generator "
                + "documentation page at https://vaadin.com/docs/flow/typescript/typescript-endpoints-generator.html.");
    }

    @Override
    public void addHandlebarHelpers(Handlebars handlebars) {
        super.addHandlebarHelpers(handlebars);
        handlebars.registerHelper("multiplelines", getMultipleLinesHelper());
        handlebars.registerHelper("getClassNameFromImports",
                getClassNameFromImportsHelper());
        handlebars.registerHelper("getModelArguments",
                getModelArgumentsHelper());
        handlebars.registerHelper("getModelFullType",
                getModelFullTypeHelper());
    }

    private Helper<String> getMultipleLinesHelper() {
        return (context, options) -> {
            Options.Buffer buffer = options.buffer();
            String[] lines = context.split("\n");
            Context parent = options.context;
            Template fn = options.fn;
            for (String line : lines) {
                buffer.append(options.apply(fn, parent.combine("@line", line)));
            }
            return buffer;
        };
    }

    private Helper<String> getClassNameFromImportsHelper() {
        return (className, options) -> getSimpleNameFromImports(className,
                (List<Map<String, String>>) options.param(0));
    }

    private Helper<CodegenProperty> getModelArgumentsHelper() {
        return (prop, options) -> getModelArguments(prop,
                (List<Map<String, String>>) options.param(0));
    }

    private Helper<CodegenProperty> getModelFullTypeHelper() {
        return (prop, options) -> getModelFullType(getSimpleNameFromImports(
                prop.datatype, (List<Map<String, String>>) options.param(0)));
    }

    /**
     * Parameter information object which is used to store body parameters in a
     * convenient way to process in the template.
     */
    private static class ParameterInformation {
        private final String name;
        private final boolean isRequired;
        private final String type;
        private final String description;

        ParameterInformation(String name, boolean isRequired, String type,
                String description) {
            this.name = name;
            this.isRequired = isRequired;
            this.type = type;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ParameterInformation that = (ParameterInformation) o;
            return Objects.equals(name, that.name)
                    && Objects.equals(type, that.type)
                    && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, description);
        }

        public boolean isRequired() {
            return isRequired;
        }
    }
}
