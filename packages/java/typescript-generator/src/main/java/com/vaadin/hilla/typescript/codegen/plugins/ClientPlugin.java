package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.ParserOutput;
import com.vaadin.hilla.typescript.codegen.TypeMapper;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import com.vaadin.hilla.typescript.parser.models.ArraySignatureModel;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import com.vaadin.hilla.typescript.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.typescript.parser.models.MethodInfoModel;
import com.vaadin.hilla.typescript.parser.models.MethodParameterInfoModel;
import com.vaadin.hilla.typescript.parser.models.SignatureModel;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Plugin that generates TypeScript client code for calling backend endpoints.
 */
public class ClientPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(ClientPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // Generate client files for each endpoint
        for (ClassInfoModel endpoint : parserOutput.getEndpoints()) {
            try {
                String clientCode = generateEndpointClient(endpoint);
                String fileName = endpoint.getSimpleName() + ".ts";
                generatedFiles.put(fileName, clientCode);
                logger.debug("Generated client for endpoint: {}",
                        endpoint.getSimpleName());
            } catch (Exception e) {
                logger.error("Failed to generate client for endpoint: {}",
                        endpoint.getSimpleName(), e);
            }
        }

        return generatedFiles;
    }

    private String generateEndpointClient(ClassInfoModel endpoint) {
        TypeScriptWriter writer = new TypeScriptWriter();

        // Collect all required type imports from method signatures
        Set<String> requiredTypes = new HashSet<>();
        for (MethodInfoModel method : endpoint.getMethods()) {
            if (isPublicMethod(method)) {
                // Collect types from return type
                collectRequiredTypes(method.getResultType(), requiredTypes);

                // Collect types from parameters
                for (MethodParameterInfoModel param : method.getParameters()) {
                    collectRequiredTypes(param.getType(), requiredTypes);
                }
            }
        }

        // Add imports
        writer.addNamedImport(List.of("ConnectClient", "EndpointRequestInit"),
                "@vaadin/hilla-frontend");

        // Add type imports for custom entity types
        for (String typeName : requiredTypes) {
            writer.addTypeImport(List.of(typeName), "./" + typeName + ".js");
        }

        // Generate client methods for each public method in the endpoint
        String methods = endpoint.getMethods().stream()
                .filter(this::isPublicMethod)
                .map(method -> generateClientMethod(endpoint, method))
                .collect(Collectors.joining("\n\n"));

        // Build the file content
        writer.appendLine("/**");
        writer.appendLine(
                " * " + endpoint.getSimpleName() + " endpoint client.");
        writer.appendLine(" */");
        writer.appendBlankLine();
        writer.appendLine("const client = new ConnectClient();");
        writer.appendBlankLine();
        writer.append(methods);

        return writer.build();
    }

    private boolean isPublicMethod(MethodInfoModel method) {
        // TODO: Check for public modifier
        // For now, include all methods that aren't constructors
        return !method.getName().equals("<init>");
    }

    private String generateClientMethod(ClassInfoModel endpoint,
            MethodInfoModel method) {
        String methodName = method.getName();
        String returnType = TypeMapper.toTypeScript(method.getResultType());

        // Build parameter list
        String paramsList = method.getParameters().stream()
                .map(this::formatParameter).collect(Collectors.joining(", "));

        String paramsWithOptions = paramsList.isEmpty()
                ? "init?: EndpointRequestInit"
                : paramsList + ", init?: EndpointRequestInit";

        // Use different templates based on whether method has parameters
        String template;
        if (paramsList.isEmpty()) {
            // Template for methods with no parameters
            template = """
                    export async function getUser(init?: EndpointRequestInit): Promise<User> {
                      return await client.call('UserEndpoint', 'getUser', {}, init);
                    }
                    """;
        } else {
            // Template for methods with parameters
            template = """
                    export async function getUser(userId: string, init?: EndpointRequestInit): Promise<User> {
                      return await client.call('UserEndpoint', 'getUser', { userId }, init);
                    }
                    """;
        }

        String code = template.replace("getUser", methodName)
                .replace("Promise<User>", "Promise<" + returnType + ">")
                .replace("'UserEndpoint'", "'" + endpoint.getSimpleName() + "'")
                .replace("'getUser'", "'" + methodName + "'");

        // Only replace parameter-specific parts if we have parameters
        if (!paramsList.isEmpty()) {
            code = code.replace("userId: string", paramsList)
                    .replace("{ userId }", buildParamsObject(method));
        }

        return code;
    }

    private String formatParameter(MethodParameterInfoModel param) {
        String paramName = param.getName();
        String paramType = TypeMapper.toTypeScript(param.getType());
        return paramName + ": " + paramType;
    }

    private String buildParamsObject(MethodInfoModel method) {
        if (method.getParameters().isEmpty()) {
            return "{}";
        }

        String paramsStr = method.getParameters().stream()
                .map(MethodParameterInfoModel::getName)
                .collect(Collectors.joining(", "));
        return "{ " + paramsStr + " }";
    }

    /**
     * Recursively collects custom types that need to be imported. Walks through
     * type signatures to find class references, including nested generic types.
     *
     * @param signature
     *            the type signature to analyze
     * @param requiredTypes
     *            the set to collect type names into
     */
    private void collectRequiredTypes(SignatureModel signature,
            Set<String> requiredTypes) {
        if (signature instanceof ArraySignatureModel) {
            // For arrays, recurse into the element type
            ArraySignatureModel arraySignature = (ArraySignatureModel) signature;
            collectRequiredTypes(arraySignature.getNestedType(), requiredTypes);
        } else if (signature instanceof ClassRefSignatureModel) {
            ClassRefSignatureModel classRef = (ClassRefSignatureModel) signature;
            String fullName = classRef.getClassInfo().getName();

            // Only add import for custom types (not standard Java types)
            if (isCustomType(fullName)) {
                requiredTypes.add(classRef.getClassInfo().getSimpleName());
            }

            // Recurse into generic type arguments (e.g., List<Entity>)
            for (SignatureModel typeArg : classRef.getTypeArguments()) {
                collectRequiredTypes(typeArg, requiredTypes);
            }
        }
        // BaseSignatureModel (primitives) don't need imports
    }

    /**
     * Checks if a type is a custom type that needs to be imported. Standard
     * Java types that map to TypeScript primitives don't need imports.
     *
     * @param fullName
     *            the fully qualified class name
     * @return true if the type needs an import
     */
    private boolean isCustomType(String fullName) {
        // Standard types that don't need imports
        return !fullName.startsWith("java.lang.")
                && !fullName.startsWith("java.util.")
                && !fullName.startsWith("java.time.");
    }

    @Override
    @NonNull
    public String getName() {
        return "ClientPlugin";
    }

    @Override
    public int getOrder() {
        return 20; // Run after ModelPlugin
    }
}
