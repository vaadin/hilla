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
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.MethodModel;
import com.vaadin.hilla.generator.model.ParameterModel;

/**
 * Writes the file which lets the client call the methods of one endpoint.
 */
public final class EndpointWriter {
    private static final String HILLA_FRONTEND = "@vaadin/hilla-frontend";
    private static final String INIT_TYPE = "EndpointRequestInit";
    private static final String INIT_PARAMETER = "init";

    private static final String METHOD = """
            export async function {{method}}({{parameters}}): Promise<{{returnType}}> {
              return {{client}}.call('{{endpoint}}', '{{method}}', {{arguments}}, {{init}});
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
        var types = new TypeWriter(imports, "");

        // The methods and their parameters are named by the Java class, so the
        // imports have to give way to them rather than the other way around
        endpoint.methods().forEach(method -> {
            imports.reserve(method.name());
            method.parameters().stream().map(ParameterModel::name)
                    .forEach(imports::reserve);
        });

        var client = imports.importDefault(clientModule, "client", false);

        var methods = endpoint.methods().stream().map(
                method -> writeMethod(endpoint, method, imports, types, client))
                .toList();

        var lines = new ArrayList<>(imports.write());
        lines.add("");
        lines.add(String.join("\n\n", methods));

        return new GeneratedFile(path, String.join("\n", lines) + "\n");
    }

    private String writeMethod(EndpointModel endpoint, MethodModel method,
            ImportRegistry imports, TypeWriter types, String client) {
        var init = initParameter(method);
        var declared = declaredParameters(method, init, imports, types);

        var written = fill(endpoint, method, String.join(", ", declared), types,
                client, init);

        // Written again with the parameters on a line each when the first line
        // came out too wide to read
        if (firstLineOf(written).length() <= Layout.MAX_WIDTH) {
            return written;
        }

        return fill(endpoint, method,
                declared.stream()
                        .collect(Collectors.joining(",\n  ", "\n  ", ",\n")),
                types, client, init);
    }

    private String fill(EndpointModel endpoint, MethodModel method,
            String parameters, TypeWriter types, String client, String init) {
        return Template.of(METHOD) //
                .with("method", method.name()) //
                .with("parameters", parameters) //
                .with("returnType", types.write(method.returnType())) //
                .with("client", client) //
                .with("endpoint", endpoint.name()) //
                .with("arguments", packParameters(method.parameters())) //
                .with("init", init) //
                .fill();
    }

    private static String firstLineOf(String method) {
        return method.lines().findFirst().orElse("");
    }

    /**
     * The name the request options go by, which gives way to a parameter of the
     * method if they happen to have the same name.
     */
    private static String initParameter(MethodModel method) {
        var names = method.parameters().stream().map(ParameterModel::name)
                .toList();
        var init = INIT_PARAMETER;

        while (names.contains(init)) {
            init = "_" + init;
        }

        return init;
    }

    /**
     * Every parameter as it is declared, the request options last.
     */
    private static List<String> declaredParameters(MethodModel method,
            String init, ImportRegistry imports, TypeWriter types) {
        var initType = imports.importNamed(HILLA_FRONTEND, INIT_TYPE, true);
        var declared = new ArrayList<String>();
        method.parameters().forEach(parameter -> declared
                .add(parameter.name() + ": " + types.write(parameter.type())));
        declared.add(init + "?: " + initType);

        return declared;
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
