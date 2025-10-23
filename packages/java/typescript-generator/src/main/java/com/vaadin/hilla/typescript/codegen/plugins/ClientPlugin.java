package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.ParserOutput;
import com.vaadin.hilla.typescript.codegen.TypeMapper;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import com.vaadin.hilla.typescript.parser.models.MethodInfoModel;
import com.vaadin.hilla.typescript.parser.models.MethodParameterInfoModel;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // Add imports
        writer.addNamedImport(List.of("EndpointRequestInit"),
                "@vaadin/hilla-frontend");

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
                .map(this::formatParameter)
                .collect(Collectors.joining(", "));

        String paramsWithOptions = paramsList.isEmpty()
                ? "init?: EndpointRequestInit"
                : paramsList + ", init?: EndpointRequestInit";

        // Build parameter names for call
        String paramNames = method.getParameters().stream()
                .map(MethodParameterInfoModel::getName)
                .collect(Collectors.joining(", "));

        // Use template
        String template = """
                export async function getUser(userId: string, init?: EndpointRequestInit): Promise<User> {
                  return await client.call('UserEndpoint', 'getUser', { userId }, init);
                }
                """;

        String code = template.replace("getUser", methodName)
                .replace("userId: string", paramsList)
                .replace("getUser(userId: string, init?: EndpointRequestInit)",
                        methodName + "(" + paramsWithOptions + ")")
                .replace("Promise<User>", "Promise<" + returnType + ">")
                .replace("'UserEndpoint'",
                        "'" + endpoint.getSimpleName() + "'")
                .replace("'getUser'", "'" + methodName + "'")
                .replace("{ userId }", buildParamsObject(method));

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
