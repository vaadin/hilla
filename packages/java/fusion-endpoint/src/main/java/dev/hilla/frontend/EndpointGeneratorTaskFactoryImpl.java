/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package dev.hilla.frontend;

import java.io.File;
import java.util.Objects;

import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

/**
 * An implementation of the EndpointGeneratorTaskFactory, which creates endpoint
 * generator tasks.
 */
public class EndpointGeneratorTaskFactoryImpl
        implements EndpointGeneratorTaskFactory {

    @Override
    public TaskGenerateEndpoint createTaskGenerateEndpoint(
            File applicationProperties, File openApi, File outputFolder,
            File frontendDirectory) {
        Objects.requireNonNull(openApi,
                "Vaadin OpenAPI file should not be null.");
        Objects.requireNonNull(outputFolder,
                "Vaadin output folder should not be null.");
        return new TaskGenerateEndpointImpl(applicationProperties, openApi,
                outputFolder, frontendDirectory);
    }

    @Override
    public TaskGenerateOpenAPI createTaskGenerateOpenAPI(File properties,
            File javaSourceFolder, ClassLoader classLoader, File output) {
        Objects.requireNonNull(javaSourceFolder,
                "Source paths should not be null.");
        Objects.requireNonNull(output,
                "OpenAPI output file should not be null.");
        Objects.requireNonNull(classLoader, "ClassLoader should not be null.");
        return new TaskGenerateOpenAPIImpl(properties, javaSourceFolder,
                classLoader, output);
    }

}
