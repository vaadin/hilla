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

    /**
     * The width beyond which the parameters of a method go on lines of their
     * own, keeping a long signature readable. Same as the width the sources of
     * the project are formatted to.
     */
    private static final int MAX_WIDTH = 120;

    /**
     * What follows the signature on its line.
     */
    private static final String BODY_START = " {";

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

        var lines = new ArrayList<>(imports.write());
        lines.add("");
        lines.add(String.join("\n\n", methods));

        return new GeneratedFile(endpoint.name() + ".ts",
                String.join("\n", lines) + "\n");
    }

    private String writeMethod(EndpointModel endpoint, MethodModel method,
            ImportRegistry imports, TypeWriter types, String client) {
        // The init parameter gives way to a parameter of the method if they
        // happen to have the same name
        var names = method.parameters().stream().map(ParameterModel::name)
                .toList();
        var init = INIT_PARAMETER;

        while (names.contains(init)) {
            init = "_" + init;
        }

        var initType = imports.importNamed(HILLA_FRONTEND, INIT_TYPE, true);
        var parameters = new ArrayList<String>();
        method.parameters().forEach(parameter -> parameters
                .add(parameter.name() + ": " + types.write(parameter.type())));
        parameters.add(init + "?: " + initType);

        var returnType = types.write(method.returnType());
        var call = "return " + client + ".call('" + endpoint.name() + "', '"
                + method.name() + "', " + packParameters(method.parameters())
                + ", " + init + ");";

        return signature(method.name(), parameters, returnType) + BODY_START
                + "\n  " + call + "\n}";
    }

    private static String signature(String name, List<String> parameters,
            String returnType) {
        var oneLine = "export async function " + name + "("
                + String.join(", ", parameters) + "): Promise<" + returnType
                + ">";

        // The body follows on the same line, so it counts towards the width
        if (oneLine.length() + BODY_START.length() <= MAX_WIDTH) {
            return oneLine;
        }

        return "export async function " + name + "(\n"
                + parameters.stream()
                        .collect(Collectors.joining(",\n  ", "  ", ",\n"))
                + "): Promise<" + returnType + ">";
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
