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
package dev.hilla;

import java.io.IOException;
import java.nio.file.Path;

import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.GeneratorProcessor;
import dev.hilla.engine.ParserProcessor;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.stereotype.Component;

import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

/**
 * Handles (re)generation of the TypeScript code.
 */
@Component
public class EndpointCodeGenerator {

    private final Path buildDirectory;
    private EndpointController endpointController;
    private ApplicationConfiguration configuration;
    private String nodeExecutable;

    private static EndpointCodeGenerator instance;

    /**
     * Creates the singleton.
     * 
     * @param context
     *            the context the application is running in
     * @param endpointController
     *            a reference to the endpoint controller
     */
    public EndpointCodeGenerator(VaadinContext context,
            EndpointController endpointController) {
        this.endpointController = endpointController;
        configuration = ApplicationConfiguration.get(context);
        buildDirectory = getBuildDirectory(configuration);
        FrontendTools tools = new FrontendTools(configuration,
                configuration.getProjectFolder());
        this.nodeExecutable = tools.getNodeBinary();
        instance = this;
    }

    /**
     * Generate or re-generate the endpoint metadata from code.
     * <p>
     * Only available in development mode.
     * 
     * @throws IOException
     */
    public static void generate() throws IOException {
        instance.doGenerate();
    }

    private void doGenerate() throws IOException {
        if (configuration.isProductionMode()) {
            throw new IllegalStateException(
                    "This method is not available in production mode");
        }

        EngineConfiguration engineConfiguration = EngineConfiguration
                .loadDirectory(buildDirectory);
        ParserProcessor parser = new ParserProcessor(engineConfiguration,
                getClass().getClassLoader());
        parser.process();
        GeneratorProcessor generator = new GeneratorProcessor(
                engineConfiguration, nodeExecutable);
        generator.process();

        this.endpointController.registerEndpoints();
    }

    private Path getBuildDirectory(ApplicationConfiguration configuration) {
        var projectFolder = configuration.getProjectFolder().toPath();
        return projectFolder.resolve(configuration.getBuildFolder());
    }
}
