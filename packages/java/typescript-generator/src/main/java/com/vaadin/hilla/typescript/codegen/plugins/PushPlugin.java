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
 * Plugin that generates TypeScript subscription methods for Flux endpoints.
 * Methods returning Flux&lt;T&gt; are generated with subscription-based API
 * instead of Promise-based API.
 */
public class PushPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(PushPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // Generate Flux subscription methods for each endpoint
        for (ClassInfoModel endpoint : parserOutput.getEndpoints()) {
            List<MethodInfoModel> fluxMethods = endpoint.getMethods().stream()
                    .filter(this::isPublicMethod).filter(this::isFluxMethod)
                    .toList();

            if (!fluxMethods.isEmpty()) {
                try {
                    String subscriptionCode = generateFluxMethods(endpoint,
                            fluxMethods);
                    String fileName = endpoint.getSimpleName()
                            + "Subscriptions.ts";
                    generatedFiles.put(fileName, subscriptionCode);
                    logger.debug(
                            "Generated Flux subscriptions for endpoint: {}",
                            endpoint.getSimpleName());
                } catch (Exception e) {
                    logger.error(
                            "Failed to generate Flux subscriptions for endpoint: {}",
                            endpoint.getSimpleName(), e);
                }
            }
        }

        return generatedFiles;
    }

    private boolean isPublicMethod(MethodInfoModel method) {
        // For now, include all methods that aren't constructors
        return !method.getName().equals("<init>");
    }

    private boolean isFluxMethod(MethodInfoModel method) {
        if (method.getResultType() instanceof ClassRefSignatureModel) {
            ClassRefSignatureModel classRef = (ClassRefSignatureModel) method
                    .getResultType();
            String className = classRef.getClassInfo().getName();
            return "reactor.core.publisher.Flux".equals(className);
        }
        return false;
    }

    private String generateFluxMethods(ClassInfoModel endpoint,
            List<MethodInfoModel> fluxMethods) {
        TypeScriptWriter writer = new TypeScriptWriter();

        // Collect all required type imports from Flux<T> type parameters
        Set<String> requiredTypes = new HashSet<>();
        for (MethodInfoModel method : fluxMethods) {
            // Get the Flux<T> type argument
            if (method.getResultType() instanceof ClassRefSignatureModel) {
                ClassRefSignatureModel fluxType = (ClassRefSignatureModel) method
                        .getResultType();
                if (!fluxType.getTypeArguments().isEmpty()) {
                    collectRequiredTypes(fluxType.getTypeArguments().get(0),
                            requiredTypes);
                }
            }

            // Also collect types from method parameters
            for (MethodParameterInfoModel param : method.getParameters()) {
                collectRequiredTypes(param.getType(), requiredTypes);
            }
        }

        // Add imports
        writer.addNamedImport(List.of("ConnectClient", "Subscription"),
                "@vaadin/hilla-frontend");
        writer.addNamedImport(List.of("EndpointRequestInit"),
                "@vaadin/hilla-frontend");

        // Add type imports for custom entity types
        for (String typeName : requiredTypes) {
            writer.addTypeImport(List.of(typeName), "./" + typeName + ".js");
        }

        // Generate subscription methods
        String methods = fluxMethods.stream()
                .map(method -> generateFluxMethod(endpoint, method))
                .collect(Collectors.joining("\n\n"));

        writer.appendLine("/**");
        writer.appendLine(" * Flux subscription methods for "
                + endpoint.getSimpleName() + ".");
        writer.appendLine(" */");
        writer.appendBlankLine();
        writer.appendLine("const client = new ConnectClient();");
        writer.appendBlankLine();
        writer.append(methods);

        return writer.build();
    }

    private String generateFluxMethod(ClassInfoModel endpoint,
            MethodInfoModel method) {
        String methodName = method.getName();

        // Get the item type from Flux<T>
        String itemType = "any";
        if (method.getResultType() instanceof ClassRefSignatureModel) {
            ClassRefSignatureModel classRef = (ClassRefSignatureModel) method
                    .getResultType();
            if (!classRef.getTypeArguments().isEmpty()) {
                itemType = TypeMapper
                        .toTypeScript(classRef.getTypeArguments().get(0));
            }
        }

        // Build parameter list
        String paramsList = method.getParameters().stream()
                .map(this::formatParameter).collect(Collectors.joining(", "));

        String paramsWithOptions = paramsList.isEmpty()
                ? "init?: EndpointRequestInit"
                : paramsList + ", init?: EndpointRequestInit";

        // Use different templates based on whether method has parameters
        String template;
        if (paramsList.isEmpty()) {
            template = """
                    export function subscribeToCountTo(onNext: (item: number) => void, onError?: (error: Error) => void, onComplete?: () => void, init?: EndpointRequestInit): Subscription<number> {
                      return client.subscribe('FluxEndpoint', 'countTo', {}, { onNext, onError, onComplete }, init);
                    }
                    """;
        } else {
            template = """
                    export function subscribeToCountTo(n: number, onNext: (item: number) => void, onError?: (error: Error) => void, onComplete?: () => void, init?: EndpointRequestInit): Subscription<number> {
                      return client.subscribe('FluxEndpoint', 'countTo', { n }, { onNext, onError, onComplete }, init);
                    }
                    """;
        }

        String callbackParams = paramsList.isEmpty() ? "" : paramsList + ", ";
        String subscribeName = "subscribeTo"
                + Character.toUpperCase(methodName.charAt(0))
                + methodName.substring(1);

        String code = template.replace("subscribeToCountTo", subscribeName)
                .replace("FluxEndpoint", endpoint.getSimpleName())
                .replace("countTo", methodName).replace("number", itemType);

        if (!paramsList.isEmpty()) {
            code = code.replace("n: number", paramsList).replace("{ n }",
                    buildParamsObject(method));
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
        return "PushPlugin";
    }

    @Override
    public int getOrder() {
        return 30; // Run after ClientPlugin
    }
}
