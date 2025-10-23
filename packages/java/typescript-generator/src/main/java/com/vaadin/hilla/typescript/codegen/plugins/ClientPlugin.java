package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    public Map<String, String> generate(@NonNull OpenAPI openAPI,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        if (openAPI.getPaths() == null) {
            logger.debug("No paths found in OpenAPI specification");
            return generatedFiles;
        }

        // Group operations by endpoint (tag)
        Map<String, List<OperationInfo>> endpointOperations = new HashMap<>();

        openAPI.getPaths().forEach((path, pathItem) -> {
            processPathItem(path, pathItem, endpointOperations);
        });

        // Generate a client file for each endpoint
        for (Map.Entry<String, List<OperationInfo>> entry : endpointOperations
                .entrySet()) {
            String endpointName = entry.getKey();
            List<OperationInfo> operations = entry.getValue();

            String clientCode = generateEndpointClient(endpointName,
                    operations);
            String fileName = endpointName + ".ts";
            generatedFiles.put(fileName, clientCode);
            logger.debug("Generated client for endpoint: {}", endpointName);
        }

        return generatedFiles;
    }

    private void processPathItem(String path, PathItem pathItem,
            Map<String, List<OperationInfo>> endpointOperations) {
        if (pathItem.getPost() != null) {
            processOperation(path, pathItem.getPost(), endpointOperations);
        }
        if (pathItem.getGet() != null) {
            processOperation(path, pathItem.getGet(), endpointOperations);
        }
    }

    private void processOperation(String path, Operation operation,
            Map<String, List<OperationInfo>> endpointOperations) {
        if (operation.getTags() == null || operation.getTags().isEmpty()) {
            return;
        }

        String tag = operation.getTags().get(0); // Use first tag as endpoint
                                                  // name
        String operationId = operation.getOperationId();

        if (operationId == null) {
            return;
        }

        OperationInfo info = new OperationInfo(operationId, path, operation);
        endpointOperations.computeIfAbsent(tag, k -> new ArrayList<>())
                .add(info);
    }

    private String generateEndpointClient(String endpointName,
            List<OperationInfo> operations) {
        TypeScriptWriter writer = new TypeScriptWriter();

        // Add imports
        writer.addNamedImport(List.of("EndpointRequestInit"),
                "@vaadin/hilla-frontend");

        // Generate methods
        String methods = operations.stream()
                .map(this::generateClientMethod).collect(Collectors.joining(
                        "\n\n"));

        // Build the file content
        writer.appendLine("/**");
        writer.appendLine(" * " + endpointName + " endpoint client.");
        writer.appendLine(" */");
        writer.appendBlankLine();
        writer.append(methods);

        return writer.build();
    }

    private String generateClientMethod(OperationInfo info) {
        String methodName = info.operationId();
        List<ParameterInfo> params = extractParameters(info.operation());
        String returnType = extractReturnType(info.operation());

        // Build parameter list
        String paramsList = params.stream()
                .map(p -> p.name() + ": " + p.type())
                .collect(Collectors.joining(", "));

        String paramsWithOptions = paramsList.isEmpty() ? "init?: EndpointRequestInit"
                : paramsList + ", init?: EndpointRequestInit";

        // Build parameter names for call
        String paramNames = params.stream().map(ParameterInfo::name)
                .collect(Collectors.joining(", "));

        // Use template
        String template = """
                export async function getUser(userId: string, init?: EndpointRequestInit): Promise<User> {
                  return await client.call('UserEndpoint', 'getUser', { userId }, init);
                }
                """;

        String code = template
                .replace("getUser", methodName)
                .replace("userId: string", paramsList)
                .replace("getUser(userId: string, init?: EndpointRequestInit)",
                        methodName + "(" + paramsWithOptions + ")")
                .replace("Promise<User>", "Promise<" + returnType + ">")
                .replace("'UserEndpoint'", "'" + extractEndpointName(info) + "'")
                .replace("'getUser'", "'" + methodName + "'")
                .replace("{ userId }", buildParamsObject(params));

        return code;
    }

    private String extractEndpointName(OperationInfo info) {
        // Extract from path like "/UserEndpoint/getUser"
        String path = info.path();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        int slashIndex = path.indexOf('/');
        return slashIndex > 0 ? path.substring(0, slashIndex) : path;
    }

    private List<ParameterInfo> extractParameters(Operation operation) {
        List<ParameterInfo> params = new ArrayList<>();

        if (operation.getRequestBody() != null
                && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            MediaType mediaType = content
                    .get("application/json");
            if (mediaType != null && mediaType.getSchema() != null) {
                Schema schema = mediaType.getSchema();
                if (schema.getProperties() != null) {
                    Map<String, Schema> properties = schema.getProperties();
                    properties.forEach((name, propSchema) -> {
                        String type = mapSchemaToTypeScript(propSchema);
                        params.add(new ParameterInfo((String) name, type));
                    });
                }
            }
        }

        return params;
    }

    private String extractReturnType(Operation operation) {
        if (operation.getResponses() == null) {
            return "void";
        }

        ApiResponse response = operation.getResponses().get("200");
        if (response == null || response.getContent() == null) {
            return "void";
        }

        MediaType mediaType = response.getContent().get("application/json");
        if (mediaType == null || mediaType.getSchema() == null) {
            return "void";
        }

        return mapSchemaToTypeScript(mediaType.getSchema());
    }

    private String buildParamsObject(List<ParameterInfo> params) {
        if (params.isEmpty()) {
            return "{}";
        }

        String paramsStr = params.stream().map(ParameterInfo::name)
                .collect(Collectors.joining(", "));
        return "{ " + paramsStr + " }";
    }

    private String mapSchemaToTypeScript(Schema schema) {
        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            int lastSlash = ref.lastIndexOf('/');
            return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
        }

        String type = schema.getType();
        if (type == null) {
            return "any";
        }

        return switch (type) {
        case "string" -> "string";
        case "number", "integer" -> "number";
        case "boolean" -> "boolean";
        case "array" -> {
            Schema items = (Schema) schema.getItems();
            if (items != null) {
                yield mapSchemaToTypeScript(items) + "[]";
            }
            yield "any[]";
        }
        case "object" -> "Record<string, any>";
        default -> "any";
        };
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

    private record OperationInfo(String operationId, String path,
            Operation operation) {
    }

    private record ParameterInfo(String name, String type) {
    }
}
