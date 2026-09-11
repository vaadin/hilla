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

    private static final String METHOD = """
            {{export}}async function {{name}}({{parameters}}): Promise<{{returnType}}> {
              return {{client}}.call('{{endpoint}}', '{{method}}', {{arguments}}, {{init}});
            }""";

    /**
     * A method sending a series of values rather than returning one, which the
     * client subscribes to instead of calling, and which has nothing to do with
     * the options of a single request.
     */
    private static final String SUBSCRIPTION = """
            {{export}}function {{name}}({{parameters}}): {{subscription}}<{{returnType}}> {
              return {{client}}.subscribe('{{endpoint}}', '{{method}}', {{arguments}});
            }""";

    /**
     * A method sharing a value with the server through a signal, which the
     * client builds rather than calling the method: the signal is what keeps
     * the value of the two in step afterwards.
     */
    private static final String SIGNAL = """
            {{export}}function {{name}}({{parameters}}): {{returnType}} {
              return new {{signal}}({{defaultValue}}{
                client: {{client}},
                endpoint: '{{endpoint}}',
                method: '{{method}}',{{params}}
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
        var path = endpoint.name() + ".ts";

        // An endpoint without a callable method still gets a file, so that
        // whatever imports it finds a module rather than nothing
        if (endpoint.methods().isEmpty()) {
            return new GeneratedFile(path, "export {};\n");
        }

        var imports = new ImportRegistry();
        var types = new TypeWriter(imports, "").withoutTypeParameters();
        var names = declaredNames(endpoint);

        // The methods and their parameters are named by the Java class, so the
        // imports have to give way to them rather than the other way around,
        // as do the types the browser has and the file writes as they are
        endpoint.methods().forEach(method -> {
            imports.reserve(names.get(method.name()));
            method.parameters().stream().map(ParameterModel::name)
                    .forEach(imports::reserve);
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

        var lines = new ArrayList<>(imports.write());
        lines.add("");
        lines.add(String.join("\n\n", methods));

        var renamed = exported(endpoint, names);

        if (!renamed.isEmpty()) {
            lines.add("");
            lines.add(renamed);
        }

        return new GeneratedFile(path, String.join("\n", lines) + "\n");
    }

    private String writeMethod(EndpointModel endpoint, MethodModel method,
            String name, ImportRegistry imports, TypeWriter types,
            ModelWriter models, String client) {
        var init = ownName(method, INIT_PARAMETER);
        var declared = declaredParameters(method, init, imports, types);

        var written = fill(endpoint, method, name, String.join(", ", declared),
                types, models, client, init, imports);

        // Written again with the parameters on a line each when the first line
        // came out too wide to read
        if (firstLineOf(written).length() <= Layout.MAX_WIDTH) {
            return written;
        }

        return fill(endpoint, method, name,
                declared.stream()
                        .collect(Collectors.joining(",\n  ", "\n  ", ",\n")),
                types, models, client, init, imports);
    }

    private String fill(EndpointModel endpoint, MethodModel method, String name,
            String parameters, TypeWriter types, ModelWriter models,
            String client, String init, ImportRegistry imports) {
        var template = Template.of(templateOf(method)) //
                // A function which is not declared under the name of the
                // method is exported under it at the end of the file instead
                .with("export", name.equals(method.name()) ? "export " : "") //
                .with("name", name) //
                .with("method", method.name()) //
                .with("parameters", parameters) //
                .with("returnType", types.write(method.returnType())) //
                .with("client", client) //
                .with("endpoint", endpoint.name());

        switch (method.kind()) {
        case CALLED ->
            template.with("arguments", packParameters(method.parameters()))
                    .with("init", init);
        case SUBSCRIBED ->
            template.with("arguments", packParameters(method.parameters()))
                    .with("subscription", imports.importNamed(HILLA_FRONTEND,
                            SUBSCRIPTION_TYPE, true));
        default -> template.with("signal", signalClass(method, imports))
                .with("defaultValue", defaultValue(method, models))
                .with("params", sharedParameters(method));
        }

        return template.fill();
    }

    /**
     * The name each method is declared under, which is its own unless
     * TypeScript reads it as a word of the language: a Java method may be
     * called delete, which no declaration can be.
     */
    private static Map<String, String> declaredNames(EndpointModel endpoint) {
        var methods = endpoint.methods().stream().map(MethodModel::name)
                .collect(Collectors.toSet());
        var names = new LinkedHashMap<String, String>();

        for (var method : methods) {
            var name = method;

            while (Names.isReserved(name)
                    || (!name.equals(method) && methods.contains(name))) {
                name = "_" + name;
            }

            names.put(method, name);
        }

        return names;
    }

    /**
     * Exports the methods which are declared under another name than their own,
     * which is how the caller still reaches them by the name of the Java
     * method.
     */
    private static String exported(EndpointModel endpoint,
            Map<String, String> names) {
        var renamed = endpoint.methods().stream().map(MethodModel::name)
                .filter(method -> !names.get(method).equals(method))
                .map(method -> names.get(method) + " as " + method).toList();

        return renamed.isEmpty() ? ""
                : renamed.stream()
                        .collect(Collectors.joining(", ", "export { ", " };"));
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
        var signal = (TypeModel.Provided) method.returnType();

        return imports.importNamed(signal.module(), signal.name(), false);
    }

    /**
     * The value a signal holds until the server says otherwise: what the caller
     * passes, falling back to the empty value of the type unless it can be
     * absent, and zero for a number, which is what a number signal starts from.
     */
    private static String defaultValue(MethodModel method, ModelWriter models) {
        if (method.kind() == MethodModel.Kind.LIST_SIGNAL) {
            return "";
        }

        if (method.kind() == MethodModel.Kind.NUMBER_SIGNAL) {
            return "0, ";
        }

        var value = sharedValue(method);
        var given = ownName(method, OPTIONS_PARAMETER) + "?.defaultValue";

        return (value.optional() ? given
                : given + " ?? " + models.className(value)
                        + ".createEmptyValue()")
                + ", ";
    }

    /**
     * The type of the value shared through the signal, which is what the signal
     * is used with.
     */
    private static TypeModel sharedValue(MethodModel method) {
        return ((TypeModel.Provided) method.returnType()).typeArguments()
                .stream().findFirst().orElseGet(() -> TypeModel.Scalar
                        .of(TypeModel.ScalarKind.NUMBER, "double"));
    }

    /**
     * What the method is called with, which the signal sends along so that the
     * server knows which value is being shared.
     */
    private static String sharedParameters(MethodModel method) {
        return method.parameters().isEmpty() ? ""
                : "\n    params: " + packParameters(method.parameters()) + ",";
    }

    private static String firstLineOf(String method) {
        return method.lines().findFirst().orElse("");
    }

    /**
     * A name the generated function needs for itself, which gives way to a
     * parameter of the method if they happen to be the same.
     */
    private static String ownName(MethodModel method, String preferred) {
        var names = method.parameters().stream().map(ParameterModel::name)
                .toList();
        var name = preferred;

        while (names.contains(name)) {
            name = "_" + name;
        }

        return name;
    }

    /**
     * Every parameter as it is declared, the request options last. A method
     * sending a series of values takes none, since the options are those of a
     * single request.
     */
    private static List<String> declaredParameters(MethodModel method,
            String init, ImportRegistry imports, TypeWriter types) {
        var declared = new ArrayList<String>();
        method.parameters().forEach(parameter -> declared
                .add(parameter.name() + ": " + types.write(parameter.type())));

        switch (method.kind()) {
        case CALLED -> declared.add(init + "?: "
                + imports.importNamed(HILLA_FRONTEND, INIT_TYPE, true));
        // The caller of a method sharing a value can say which value to start
        // from, which a number signal decides itself
        case VALUE_SIGNAL ->
            declared.add(ownName(method, OPTIONS_PARAMETER) + "?: "
                    + imports.importNamed(signalsModule(method),
                            SIGNAL_OPTIONS_TYPE, true)
                    + "<" + types.write(sharedValue(method)) + ">");
        default -> {
        }
        }

        return declared;
    }

    private static String signalsModule(MethodModel method) {
        return ((TypeModel.Provided) method.returnType()).module();
    }

    /**
     * The parameters are sent as one object, keyed by the name the server knows
     * them by.
     */
    private static String packParameters(List<ParameterModel> parameters) {
        if (parameters.isEmpty()) {
            return "{}";
        }

        return parameters.stream().map(ParameterModel::name)
                .collect(Collectors.joining(", ", "{ ", " }"));
    }
}
