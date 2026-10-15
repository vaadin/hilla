/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.GeneratorProcessor;
import com.vaadin.hilla.engine.ParserProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

/**
 * Handles (re)generation of the TypeScript code.
 */
@Component
public class EndpointCodeGenerator {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointCodeGenerator.class);

    private final EndpointController endpointController;
    private final VaadinContext context;
    private Path buildDirectory;

    private ApplicationConfiguration configuration;
    private String nodeExecutable;
    private Set<String> classesUsedInOpenApi = null;

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
        this.context = context;
    }

    /**
     * Gets the singleton instance.
     */
    public static EndpointCodeGenerator getInstance() {
        return ApplicationContextProvider.getApplicationContext()
                .getBean(EndpointCodeGenerator.class);
    }

    /**
     * Re-generates the endpoint TypeScript and re-registers the endpoints in
     * Java.
     *
     * @throws IOException
     *             if something went wrong
     */
    public void update() throws IOException {
        initIfNeeded();
        if (configuration.isProductionMode()) {
            throw new IllegalStateException(
                    "This method is not available in production mode");
        }

        EngineConfiguration engineConfiguration = EngineConfiguration
                .loadDirectory(buildDirectory);
        ParserProcessor parser = new ParserProcessor(engineConfiguration,
                getClass().getClassLoader(), false);
        parser.process();
        GeneratorProcessor generator = new GeneratorProcessor(
                engineConfiguration, nodeExecutable, false);
        generator.process();

        OpenAPIUtil.getCurrentOpenAPIPath(buildDirectory, false)
                .ifPresent(openApiPath -> {
                    try {
                        this.endpointController
                                .registerEndpoints(openApiPath.toUri().toURL());
                    } catch (IOException e) {
                        LOGGER.error(
                                "Endpoints could not be registered due to an exception: ",
                                e);
                    }
                });
    }

    private void initIfNeeded() {
        if (configuration == null) {
            configuration = ApplicationConfiguration.get(context);

            Path projectFolder = configuration.getProjectFolder().toPath();
            buildDirectory = projectFolder
                    .resolve(configuration.getBuildFolder());

            FrontendTools tools = new FrontendTools(configuration,
                    configuration.getProjectFolder());
            nodeExecutable = tools.getNodeBinary();
        }
    }

    public Optional<Set<String>> getClassesUsedInOpenApi() throws IOException {
        if (classesUsedInOpenApi == null) {
            initIfNeeded();
            OpenAPIUtil.getCurrentOpenAPIPath(buildDirectory, false)
                    .ifPresent(openApiPath -> {
                        if (openApiPath.toFile().exists()) {
                            try {
                                classesUsedInOpenApi = OpenAPIUtil
                                        .findOpenApiClasses(
                                                Files.readString(openApiPath));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            LOGGER.debug(
                                    "No OpenAPI file is available yet ...");
                        }
                    });
        }
        return Optional.ofNullable(classesUsedInOpenApi);
    }
}
