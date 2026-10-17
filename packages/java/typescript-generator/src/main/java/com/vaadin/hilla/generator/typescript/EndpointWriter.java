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
            async function {{method}}({{parameters}}): Promise<{{returnType}}> {
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
        var imports = new ImportRegistry();
        var types = new TypeWriter(imports, "");

        // The parameters are named by the Java method, so the imports have to
        // give way to them rather than the other way around
        endpoint.methods().stream().map(MethodModel::parameters)
                .flatMap(List::stream).map(ParameterModel::name)
                .forEach(imports::reserve);

        var client = imports.importDefault(clientModule, "client", false);

        var methods = endpoint.methods().stream().map(
                method -> writeMethod(endpoint, method, imports, types, client))
                .toList();

        var exports = GeneratedFile.exportList(
                endpoint.methods().stream().map(MethodModel::name).toList());

        return GeneratedFile.of(endpoint.name(), imports,
                String.join("\n\n", methods) + "\n\n" + exports);
    }

    private String writeMethod(EndpointModel endpoint, MethodModel method,
            ImportRegistry imports, TypeWriter types, String client) {
        var init = initParameter(method);
        var declared = declaredParameters(method, init, imports, types);

        return Template.of(METHOD) //
                .with("method", method.name()) //
                .with("parameters", String.join(", ", declared)) //
                .with("returnType", types.write(method.returnType())) //
                .with("client", client) //
                .with("endpoint", endpoint.name()) //
                .with("arguments", packParameters(method.parameters())) //
                .with("init", init) //
                .fill();
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
