/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.frontend;

import java.io.File;
import java.util.Objects;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateFusion;
import com.vaadin.fusion.generator.MainGenerator;

import static com.vaadin.fusion.generator.ClientAPIGenerator.CUSTOM_CONNECT_CLIENT_NAME;

/**
 * Starts the generation of TS files for endpoints.
 */
public class TaskGenerateFusionImpl extends AbstractTaskFusionGenerator
        implements TaskGenerateFusion {

    private final File frontendDirectory;
    private final File openApi;
    private final File outputFolder;

    TaskGenerateFusionImpl(File applicationProperties, File openApi,
            File outputFolder, File frontendDirectory) {
        super(applicationProperties);
        Objects.requireNonNull(openApi,
                "Vaadin OpenAPI file should not be null.");
        Objects.requireNonNull(outputFolder,
                "Vaadin output folder should not be null.");
        this.openApi = openApi;
        this.outputFolder = outputFolder;
        this.frontendDirectory = frontendDirectory;
    }

    @Override
    public void execute() throws ExecutionFailedException {
        File customConnectClient = new File(frontendDirectory,
                CUSTOM_CONNECT_CLIENT_NAME);
        String customName = customConnectClient.exists()
                ? ("../" + CUSTOM_CONNECT_CLIENT_NAME)
                : null;

        new MainGenerator(openApi, outputFolder, readApplicationProperties(),
                customName).start();
    }
}
