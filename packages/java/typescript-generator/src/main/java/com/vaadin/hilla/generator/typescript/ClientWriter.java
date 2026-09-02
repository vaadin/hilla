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

/**
 * Writes the client the generated endpoints call the server with, which is
 * generated unless the application provides one of its own.
 */
public final class ClientWriter {
    /**
     * The module of the generated client, relative to the output folder.
     */
    public static final String MODULE = "connect-client.default";

    /**
     * The module of the client an application provides itself, which sits next
     * to the output folder rather than in it.
     */
    public static final String CUSTOM_MODULE = "../connect-client";

    /**
     * How the generated endpoints, which sit in the output folder, refer to the
     * generated client.
     */
    public static final String MODULE_SPECIFIER = "./" + MODULE + ".js";

    /**
     * How the generated endpoints refer to a client the application provides
     * itself, which sits next to the output folder.
     */
    public static final String CUSTOM_MODULE_SPECIFIER = CUSTOM_MODULE + ".js";

    public GeneratedFile write() {
        var imports = new ImportRegistry();
        var connectClient = imports.importNamed("@vaadin/hilla-frontend",
                "ConnectClient", false);

        var lines = new java.util.ArrayList<>(imports.write());
        lines.add("");
        lines.add("const client = new " + connectClient
                + "({ prefix: 'connect' });");
        lines.add("");
        lines.add("export default client;");

        return new GeneratedFile(MODULE + ".ts",
                String.join("\n", lines) + "\n");
    }
}
