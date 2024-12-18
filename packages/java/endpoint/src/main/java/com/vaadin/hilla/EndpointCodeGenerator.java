/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.hilla;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.GeneratorProcessor;
import com.vaadin.hilla.engine.ParserProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.stereotype.Component;

/**
 * Handles (re)generation of the TypeScript code.
 */
@Component
public class EndpointCodeGenerator {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointCodeGenerator.class);

    private final EndpointController endpointController;
    private final VaadinContext context;

    private ApplicationConfiguration configuration;
    private Set<String> classesUsedInOpenApi = null;
    private EngineConfiguration engineConfiguration;

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
     */
    public void update() {
        initIfNeeded();
        if (configuration.isProductionMode()) {
            throw new IllegalStateException(
                    "This method is not available in production mode");
        }

        ApplicationContextProvider.runOnContext(applicationContext -> {
            List<Class<?>> browserCallables = engineConfiguration
                    .getEndpointAnnotations().stream()
                    .map(applicationContext::getBeansWithAnnotation)
                    .map(Map::values).flatMap(Collection::stream)
                    // maps to original class when proxies are found
                    // (also converts to class in all cases)
                    .map(AopProxyUtils::ultimateTargetClass).distinct()
                    .collect(Collectors.toList());
            ParserProcessor parser = new ParserProcessor(engineConfiguration);
            parser.process(browserCallables);

            GeneratorProcessor generator = new GeneratorProcessor(
                    engineConfiguration);
            generator.process();

            try {
                var openApiPath = engineConfiguration.getOpenAPIFile();
                this.endpointController
                        .registerEndpoints(openApiPath.toUri().toURL());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void initIfNeeded() {
        if (configuration == null) {
            configuration = ApplicationConfiguration.get(context);

            var frontendTools = new FrontendTools(configuration,
                    configuration.getProjectFolder());
            engineConfiguration = new EngineConfiguration.Builder()
                    .baseDir(configuration.getProjectFolder().toPath())
                    .buildDir(configuration.getBuildFolder())
                    .outputDir(
                            FrontendUtils
                                    .getFrontendGeneratedFolder(
                                            configuration.getFrontendFolder())
                                    .toPath())
                    .productionMode(false)
                    .nodeCommand(frontendTools.getNodeBinary()).build();
        }
    }

    public Optional<Set<String>> getClassesUsedInOpenApi() throws IOException {
        if (classesUsedInOpenApi == null) {
            initIfNeeded();
            var conf = EngineConfiguration.getDefault();
            var openApiPath = conf.getOpenAPIFile();
            if (openApiPath != null && openApiPath.toFile().exists()) {
                try {
                    classesUsedInOpenApi = OpenAPIUtil
                            .findOpenApiClasses(Files.readString(openApiPath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                LOGGER.debug("No OpenAPI file is available yet ...");
            }
        }
        return Optional.ofNullable(classesUsedInOpenApi);
    }
}
