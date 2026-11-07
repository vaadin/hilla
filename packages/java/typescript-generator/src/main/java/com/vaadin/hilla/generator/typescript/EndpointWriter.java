/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.generator.typescript;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.MethodModel;
import com.vaadin.hilla.generator.model.ParameterModel;
import com.vaadin.hilla.generator.model.TypeModel;

/**
 * Writes the file which lets the client call the methods of one endpoint.
 */
public final class EndpointWriter {
    private static final String HILLA_FRONTEND = "@vaadin/hilla-frontend";
    private static final String INIT_TYPE = "EndpointRequestInit";
    private static final String INIT_PARAMETER = "init";
    private static final String SUBSCRIPTION_TYPE = "Subscription";
    private static final String SIGNAL_OPTIONS_TYPE = "SignalMethodOptions";
    private static final String OPTIONS_PARAMETER = "options";

    /**
     * What the file of an endpoint without a method the browser can call says
     * in place of the methods, naming both ways a method is left out: not being
     * public, and being inherited from a type which is not exposed. A block
     * comment rather than line comments, so that the export after it cannot end
     * up commented out whatever the line breaks. The Java class is not named,
     * so that its package does not end up in the frontend bundle.
     */
    private static final String NO_METHODS = """
            /*
             * This endpoint has no methods the browser can call. Only the public
             * methods declared in the class itself or in a superclass or
             * interface annotated with @EndpointExposed are generated.
             */""";

    private static final String METHOD = """
            async function {{name}}({{parameters}}): Promise<{{returnType}}> {
              return {{client}}.call('{{endpoint}}', '{{method}}', {{arguments}}, {{init}});
            }""";

    /**
     * A method sending a series of values rather than returning one, which the
     * client subscribes to instead of calling, and which has nothing to do with
     * the options of a single request.
     */
    private static final String SUBSCRIPTION = """
            function {{name}}({{parameters}}): {{subscription}}<{{returnType}}> {
              return {{client}}.subscribe('{{endpoint}}', '{{method}}', {{arguments}});
            }""";

    /**
     * A method sharing a value with the server through a signal, which the
     * client builds rather than calling the method: the signal is what keeps
     * the value of the two in step afterwards.
     */
    private static final String SIGNAL = """
            function {{name}}({{parameters}}): {{returnType}} {
              return new {{signal}}({{defaultValue}}{
                client: {{client}},
                endpoint: '{{endpoint}}',
                method: '{{method}}',
                {{params}}
              });
            }""";

    private final String clientModule;

    /**
     * @param clientModule
     *            the module exporting the client to call the server with, which
     *            is the generated one unless the application has one of its own
     */
    public EndpointWriter(String clientModule) {
        this.clientModule = clientModule;
    }

    public GeneratedFile write(EndpointModel endpoint) {
        // An endpoint without a callable method still gets a file, so that
        // whatever imports it finds a module rather than nothing, and so that
        // the developer looking for the methods learns why there are none
        if (endpoint.methods().isEmpty()) {
            return GeneratedFile.of(endpoint.name(), new ImportRegistry(),
                    NO_METHODS + "\n" + GeneratedFile.exportList(List.of()));
        }

        var imports = new ImportRegistry();
        var types = new TypeWriter(imports, "").withoutTypeParameters();
        var names = declaredNames(endpoint);

        // The methods and their parameters are named by the Java class, so the
        // imports have to give way to them rather than the other way around,
        // as do the types the browser has and the file writes as they are
        endpoint.methods().forEach(method -> {
            imports.reserve(names.get(method.name()));
            declaredNames(method).values().forEach(imports::reserve);
            types.reserveProvided(method.returnType());
            method.parameters().forEach(
                    parameter -> types.reserveProvided(parameter.type()));
        });

        var client = imports.importDefault(clientModule, "client", false);
        var models = new ModelWriter(imports, "");

        var methods = endpoint.methods().stream()
                .map(method -> writeMethod(endpoint, method,
                        names.get(method.name()), imports, types, models,
                        client))
                .toList();

        // A function which is not declared under the name of the method is
        // exported under it instead
        var exports = GeneratedFile
                .exportList(endpoint.methods().stream().map(MethodModel::name)
                        .map(method -> names.get(method).equals(method) ? method
                                : names.get(method) + " as " + method)
                        .toList());

        return GeneratedFile.of(endpoint.name(), imports,
                String.join("\n\n", methods) + "\n\n" + exports);
    }

    private String writeMethod(EndpointModel endpoint, MethodModel method,
            String name, ImportRegistry imports, TypeWriter types,
            ModelWriter models, String client) {
        var parameters = declaredNames(method);
        var init = ownName(parameters, INIT_PARAMETER);
        var declared = declaredParameters(method, parameters, init, imports,
                types);

        var template = Template.of(templateOf(method)) //
                .with("name", name) //
                .with("method", method.name()) //
                .with("parameters", String.join(", ", declared)) //
                .with("returnType", types.write(method.returnType())) //
                .with("client", client) //
                .with("endpoint", endpoint.name());

        switch (method.kind()) {
        case CALLED ->
            template.with("arguments", packParameters(method, parameters))
                    .with("init", init);
        case SUBSCRIBED ->
            template.with("arguments", packParameters(method, parameters))
                    .with("subscription", imports.importNamed(HILLA_FRONTEND,
                            SUBSCRIPTION_TYPE, true));
        default -> template.with("signal", signalClass(method, imports))
                .with("defaultValue", defaultValue(method, parameters, models))
                .with("params", sharedParameters(method, parameters));
        }

        return template.fill();
    }

    /**
     * The name each method is declared under, which is its own unless
     * TypeScript reads it as a word of the language: a Java method may be
     * called delete, which no declaration can be.
     */
    private static Map<String, String> declaredNames(EndpointModel endpoint) {
        return declaredNames(endpoint.methods().stream().map(MethodModel::name)
                .collect(Collectors.toSet()));
    }

    /**
     * The name each parameter of a method is declared under, which is its own
     * unless TypeScript reads it as a word of the language: a function cannot
     * take a parameter called delete, although the server knows it by that
     * name.
     */
    private static Map<String, String> declaredNames(MethodModel method) {
        return declaredNames(method.parameters().stream()
                .map(ParameterModel::name).collect(Collectors.toSet()));
    }

    /**
     * Each of the given names by the name it is declared under, which is one
     * nothing else goes by: a name taken instead of a word of the language is
     * left to whoever has it already.
     */
    private static Map<String, String> declaredNames(Set<String> names) {
        var declared = new LinkedHashMap<String, String>();

        for (var name : names) {
            declared.put(name, Names.free(name, own -> Names.isReserved(own)
                    || (!own.equals(name) && names.contains(own))));
        }

        return declared;
    }

    private static String templateOf(MethodModel method) {
        return switch (method.kind()) {
        case CALLED -> METHOD;
        case SUBSCRIBED -> SUBSCRIPTION;
        default -> SIGNAL;
        };
    }

    /**
     * The signal the client builds, which is the type the method returns, used
     * as the value it is rather than as a type.
     */
    private static String signalClass(MethodModel method,
            ImportRegistry imports) {
        return imports.importNamed(method.signal().module(),
                method.signal().name(), false);
    }

    /**
     * The value a signal holds until the server says otherwise: what the caller
     * passes, falling back to the empty value of the type unless it can be
     * absent, and zero for a number, which is what a number signal starts from.
     */
    private static String defaultValue(MethodModel method,
            Map<String, String> parameterNames, ModelWriter models) {
        // A list or a map starts out empty, so there is nothing to give
        if (method.kind() == MethodModel.Kind.LIST_SIGNAL
                || method.kind() == MethodModel.Kind.MAP_SIGNAL) {
            return "";
        }

        if (method.kind() == MethodModel.Kind.NUMBER_SIGNAL) {
            return "0, ";
        }

        var value = sharedValue(method);
        var given = ownName(parameterNames, OPTIONS_PARAMETER)
                + "?.defaultValue";

        return (value.optional() ? given
                : given + " ?? " + models.defaultValue(value)) + ", ";
    }

    /**
     * The type of the value shared through the signal, which is what the signal
     * is used with.
     */
    private static TypeModel sharedValue(MethodModel method) {
        return method.signal().typeArguments().stream().findFirst()
                .orElseGet(() -> TypeModel.Scalar
                        .of(TypeModel.ScalarKind.NUMBER, "double"));
    }

    /**
     * What the method is called with, which the signal sends along so that the
     * server knows which value is being shared.
     */
    private static String sharedParameters(MethodModel method,
            Map<String, String> parameterNames) {
        return method.parameters().isEmpty() ? ""
                : "params: " + packParameters(method, parameterNames) + ",";
    }

    /**
     * A name the generated function needs for itself, which gives way to a
     * parameter of the method if they happen to be the same.
     */
    private static String ownName(Map<String, String> parameterNames,
            String preferred) {
        return Names.free(preferred, parameterNames::containsValue);
    }

    /**
     * Every parameter as it is declared, the request options last. A method
     * sending a series of values takes none, since the options are those of a
     * single request.
     */
    private static List<String> declaredParameters(MethodModel method,
            Map<String, String> parameterNames, String init,
            ImportRegistry imports, TypeWriter types) {
        var declared = new ArrayList<String>();
        method.parameters().forEach(
                parameter -> declared.add(parameterNames.get(parameter.name())
                        + ": " + types.write(parameter.type())));

        switch (method.kind()) {
        case CALLED -> declared.add(init + "?: "
                + imports.importNamed(HILLA_FRONTEND, INIT_TYPE, true));
        // The caller of a method sharing a value can say which value to start
        // from, which a number signal decides itself
        case VALUE_SIGNAL ->
            declared.add(ownName(parameterNames, OPTIONS_PARAMETER) + "?: "
                    + imports.importNamed(method.signal().module(),
                            SIGNAL_OPTIONS_TYPE, true)
                    + "<" + types.write(sharedValue(method)) + ">");
        default -> {
        }
        }

        return declared;
    }

    /**
     * The parameters are sent as one object, keyed by the name the server knows
     * them by, which a parameter declared under another name is written as.
     */
    private static String packParameters(MethodModel method,
            Map<String, String> parameterNames) {
        if (method.parameters().isEmpty()) {
            return "{}";
        }

        return method.parameters().stream().map(ParameterModel::name)
                .map(name -> name.equals(parameterNames.get(name)) ? name
                        : name + ": " + parameterNames.get(name))
                .collect(Collectors.joining(", ", "{ ", " }"));
    }
}
