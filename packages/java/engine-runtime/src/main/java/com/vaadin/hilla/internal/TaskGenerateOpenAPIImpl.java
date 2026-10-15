/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.function.Function;

import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.EndpointCodeGenerator;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.ParserProcessor;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

import org.springframework.aop.framework.AopProxyUtils;

/**
 * Generate OpenAPI json file for Vaadin Endpoints.
 */
public class TaskGenerateOpenAPIImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateOpenAPI {

    /**
     * Create a task for generating OpenAPI spec.
     *
     * @param engineConfiguration
     *            Hilla engine configuration instance
     */
    TaskGenerateOpenAPIImpl(EngineConfiguration engineConfiguration) {
        super(engineConfiguration);
    }

    /**
     * Run Java class parser.
     *
     * @throws ExecutionFailedException
     */
    @Override
    public void execute() throws ExecutionFailedException {
        var engineConfiguration = getEngineConfiguration();
        if (engineConfiguration.isProductionMode()) {
            var browserCallables = engineConfiguration
                    .getBrowserCallableFinder().findBrowserCallables();
            var processor = new ParserProcessor(engineConfiguration);
            processor.process(browserCallables);
        } else {
            ApplicationContextProvider.runOnContext(applicationContext -> {
                List<Class<?>> browserCallables = EndpointCodeGenerator
                        .findBrowserCallables(engineConfiguration,
                                applicationContext);
                var processor = new ParserProcessor(engineConfiguration);
                processor.process(browserCallables);
            });
        }
    }
}
