package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.ParserOutput;
import com.vaadin.hilla.typescript.codegen.TypeMapper;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import com.vaadin.hilla.typescript.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.typescript.parser.models.MethodInfoModel;
import com.vaadin.hilla.typescript.parser.models.MethodParameterInfoModel;
import com.vaadin.hilla.typescript.parser.models.SignatureModel;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plugin that generates React Signals integration for endpoints. For endpoints
 * with Signal return types, this plugin regenerates the endpoint client file
 * with transformed Signal methods.
 */
public class SignalsPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(SignalsPlugin.class);

    private static final List<String> SIGNAL_TYPES = List.of("NumberSignal",
            "ValueSignal", "ListSignal", "Signal");

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // For each endpoint, check if it has any Signal methods
        for (ClassInfoModel endpoint : parserOutput.getEndpoints()) {
            if (hasSignalMethods(endpoint)) {
                try {
                    // Regenerate the entire client file with Signal methods
                    // transformed
                    String clientCode = generateEndpointClientWithSignals(
                            endpoint);
                    String fileName = endpoint.getSimpleName() + ".ts";
                    generatedFiles.put(fileName, clientCode);
                    logger.debug(
                            "Generated client with signals for endpoint: {}",
                            endpoint.getSimpleName());
                } catch (Exception e) {
                    logger.error("Failed to generate signals for endpoint: {}",
                            endpoint.getSimpleName(), e);
                }
            }
        }

        return generatedFiles;
    }

    private boolean hasSignalMethods(ClassInfoModel endpoint) {
        return endpoint.getMethods().stream().filter(this::isPublicMethod)
                .anyMatch(method -> isSignalMethod(method));
    }

    private boolean isPublicMethod(MethodInfoModel method) {
        return !method.getName().equals("<init>");
    }

    private boolean isSignalMethod(MethodInfoModel method) {
        SignatureModel returnType = method.getResultType();
        if (returnType instanceof ClassRefSignatureModel) {
            ClassRefSignatureModel classRef = (ClassRefSignatureModel) returnType;
            String className = classRef.getClassInfo().getSimpleName();
            return SIGNAL_TYPES.contains(className);
        }
        return false;
    }

    private String generateEndpointClientWithSignals(ClassInfoModel endpoint) {
        TypeScriptWriter writer = new TypeScriptWriter();

        // Add standard imports
        writer.addNamedImport(List.of("EndpointRequestInit"),
                "@vaadin/hilla-frontend");

        // Determine which signal types and helper types are needed
        boolean needsNumberSignal = false;
        boolean needsValueSignal = false;
        boolean needsListSignal = false;
        boolean needsSignal = false;

        for (MethodInfoModel method : endpoint.getMethods()) {
            if (!isPublicMethod(method)) {
                continue;
            }
            if (isSignalMethod(method)) {
                String signalType = getSignalTypeName(method);
                needsNumberSignal |= signalType.equals("NumberSignal");
                needsValueSignal |= signalType.startsWith("ValueSignal");
                needsListSignal |= signalType.startsWith("ListSignal");
                needsSignal |= signalType.equals("Signal");
            }
        }

        // Add signal imports
        List<String> signalImports = new java.util.ArrayList<>();
        if (needsNumberSignal) {
            signalImports.add("NumberSignal");
        }
        if (needsValueSignal) {
            signalImports.add("ValueSignal");
            signalImports.add("SignalMethodOptions");
        }
        if (needsListSignal) {
            signalImports.add("ListSignal");
        }
        if (needsSignal) {
            signalImports.add("Signal");
        }

        if (!signalImports.isEmpty()) {
            writer.addNamedImport(signalImports, "@vaadin/hilla-react-signals");
        }

        // Add client import placeholder (will be added by TypeScriptWriter
        // automatically)
        // In real implementation, this would come from connect-client

        // Generate methods (both regular and signal)
        String methods = endpoint.getMethods().stream()
                .filter(this::isPublicMethod)
                .map(method -> isSignalMethod(method)
                        ? generateSignalMethod(endpoint, method)
                        : generateRegularMethod(endpoint, method))
                .collect(Collectors.joining("\n\n"));

        // Build file content
        writer.appendLine("/**");
        writer.appendLine(
                " * " + endpoint.getSimpleName() + " endpoint client.");
        writer.appendLine(" */");
        writer.appendBlankLine();

        // Add a placeholder for client import
        writer.appendLine(
                "const client = { call: () => {} }; // TODO: Import actual client");
        writer.appendBlankLine();

        writer.append(methods);

        return writer.build();
    }

    private String getSignalTypeName(MethodInfoModel method) {
        SignatureModel returnType = method.getResultType();
        if (returnType instanceof ClassRefSignatureModel) {
            ClassRefSignatureModel classRef = (ClassRefSignatureModel) returnType;
            String className = classRef.getClassInfo().getSimpleName();

            // For generic signals like ValueSignal<T>, include the type
            // argument
            if (className.equals("ValueSignal")
                    || className.equals("ListSignal")) {
                if (!classRef.getTypeArguments().isEmpty()) {
                    String typeArg = TypeMapper
                            .toTypeScript(classRef.getTypeArguments().get(0));
                    return className + "<" + typeArg + ">";
                }
            }

            return className;
        }
        return "Signal";
    }

    private String generateRegularMethod(ClassInfoModel endpoint,
            MethodInfoModel method) {
        // Generate regular Promise-based client method (same as ClientPlugin)
        String methodName = method.getName();
        String returnType = TypeMapper.toTypeScript(method.getResultType());

        String paramsList = method.getParameters().stream()
                .map(this::formatParameter).collect(Collectors.joining(", "));

        String template;
        if (paramsList.isEmpty()) {
            template = """
                    export async function getUser(init?: EndpointRequestInit): Promise<User> {
                      return await client.call('UserEndpoint', 'getUser', {}, init);
                    }
                    """;
        } else {
            template = """
                    export async function getUser(userId: string, init?: EndpointRequestInit): Promise<User> {
                      return await client.call('UserEndpoint', 'getUser', { userId }, init);
                    }
                    """;
        }

        String code = template.replace("getUser", methodName)
                .replace("Promise<User>", "Promise<" + returnType + ">")
                .replace("'UserEndpoint'",
                        "'" + endpoint.getSimpleName() + "'");

        if (!paramsList.isEmpty()) {
            code = code.replace("userId: string", paramsList)
                    .replace("{ userId }", buildParamsObject(method));
        }

        return code;
    }

    private String generateSignalMethod(ClassInfoModel endpoint,
            MethodInfoModel method) {
        String methodName = method.getName();
        String signalType = getSignalTypeName(method);

        // Build parameter list (without init parameter)
        String paramsList = method.getParameters().stream()
                .map(this::formatParameter).collect(Collectors.joining(", "));

        // Determine if this is NumberSignal (simple), ValueSignal (needs
        // options), or ListSignal
        boolean isNumberSignal = signalType.equals("NumberSignal");
        boolean isValueSignal = signalType.startsWith("ValueSignal");
        boolean isListSignal = signalType.startsWith("ListSignal");

        String template;
        String defaultValue = "";

        if (isNumberSignal) {
            // NumberSignal: always use 0 as default
            template = """
                    export function counter(): NumberSignal {
                      return new NumberSignal(0, {
                        client,
                        endpoint: 'NumberSignalService',
                        method: 'counter'
                      });
                    }
                    """;
            defaultValue = "0";
        } else if (isValueSignal) {
            // ValueSignal: needs options parameter with defaultValue
            template = """
                    export function stringSignal(options?: SignalMethodOptions<string>): ValueSignal<string> {
                      return new ValueSignal(options?.defaultValue, {
                        client,
                        endpoint: 'Service',
                        method: 'stringSignal'
                      });
                    }
                    """;

            // Add options parameter if there are other parameters
            if (!paramsList.isEmpty()) {
                paramsList = paramsList + ", options?: SignalMethodOptions<"
                        + extractTypeArgument(signalType) + ">";
            } else {
                paramsList = "options?: SignalMethodOptions<"
                        + extractTypeArgument(signalType) + ">";
            }
        } else {
            // ListSignal or base Signal: no default value
            template = """
                    export function listSignal(): ListSignal<string> {
                      return new ListSignal({
                        client,
                        endpoint: 'Service',
                        method: 'listSignal'
                      });
                    }
                    """;
        }

        // Build the params object for the signal constructor
        String paramsObject = "";
        if (!method.getParameters().isEmpty()) {
            String paramNames = method.getParameters().stream()
                    .map(MethodParameterInfoModel::getName)
                    .collect(Collectors.joining(", "));
            paramsObject = ",\n    params: { " + paramNames + " }";
        }

        // Replace template placeholders
        String code = template;

        // Replace function name
        if (isNumberSignal) {
            code = code.replace("counter", methodName);
            code = code.replace("'counter'", "'" + methodName + "'");
        } else if (isValueSignal) {
            code = code.replace("stringSignal", methodName);
            code = code.replace("'stringSignal'", "'" + methodName + "'");
        } else {
            code = code.replace("listSignal", methodName);
            code = code.replace("'listSignal'", "'" + methodName + "'");
        }

        // Replace endpoint name
        code = code.replace("'NumberSignalService'",
                "'" + endpoint.getSimpleName() + "'");
        code = code.replace("'Service'", "'" + endpoint.getSimpleName() + "'");

        // Replace return type
        if (isNumberSignal) {
            code = code.replace("(): NumberSignal",
                    "(" + paramsList + "): " + signalType);
        } else if (isValueSignal) {
            code = code.replace("(options?: SignalMethodOptions<string>)",
                    "(" + paramsList + ")");
            code = code.replace("ValueSignal<string>", signalType);
        } else {
            code = code.replace("(): ListSignal<string>",
                    "(" + paramsList + "): " + signalType);
        }

        // Add params object if method has parameters
        if (!paramsObject.isEmpty()) {
            // Replace the closing of the options object to add params
            // We replace the newline before the closing brace
            code = code.replace("\n  });", paramsObject + "\n  });");
        }

        return code;
    }

    private String extractTypeArgument(String signalType) {
        // Extract T from ValueSignal<T> or ListSignal<T>
        if (signalType.contains("<")) {
            int start = signalType.indexOf('<') + 1;
            int end = signalType.lastIndexOf('>');
            return signalType.substring(start, end);
        }
        return "any";
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
        return "SignalsPlugin";
    }

    @Override
    public int getOrder() {
        return 40; // Run after ClientPlugin to overwrite files with Signal
                   // methods
    }
}
