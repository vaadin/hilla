/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.server.frontend.fusion;

import java.io.File;
import java.util.Objects;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateConnect;
import com.vaadin.flow.server.connect.generator.VaadinConnectClientGenerator;
import com.vaadin.flow.server.connect.generator.VaadinConnectTsGenerator;

import static com.vaadin.flow.server.connect.generator.VaadinConnectClientGenerator.CONNECT_CLIENT_NAME;
import static com.vaadin.flow.server.connect.generator.VaadinConnectClientGenerator.CUSTOM_CONNECT_CLIENT_NAME;

/**
 * Generate the Vaadin TS files for endpoints, and the Client API file.
 */
public class TaskGenerateConnectImpl extends AbstractTaskConnectGenerator implements TaskGenerateConnect {

    private final File outputFolder;
    private final File openApi;
    private final File connectClientFile;
    private final File frontendDirectory;

    TaskGenerateConnectImpl(File applicationProperties, File openApi,
                        File outputFolder, File frontendDirectory) {
        super(applicationProperties);
        Objects.requireNonNull(openApi,
                "Vaadin OpenAPI file should not be null.");
        Objects.requireNonNull(outputFolder,
                "Vaadin output folder should not be null.");
        this.openApi = openApi;
        this.outputFolder = outputFolder;
        this.connectClientFile = new File(outputFolder, CONNECT_CLIENT_NAME);
        this.frontendDirectory = frontendDirectory;
    }

    @Override
    public void execute() throws ExecutionFailedException {
        File customConnectClient = new File(frontendDirectory, CUSTOM_CONNECT_CLIENT_NAME);
        String customName = customConnectClient.exists() ? ("../" + CUSTOM_CONNECT_CLIENT_NAME) : null;
        if (VaadinConnectTsGenerator.launch(openApi, outputFolder, customName)) {
            new VaadinConnectClientGenerator(readApplicationProperties())
                    .generateVaadinConnectClientFile(connectClientFile.toPath());
        }
    }
}
