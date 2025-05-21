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
import com.vaadin.hilla.engine.EngineAutoConfiguration;
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
    TaskGenerateOpenAPIImpl(EngineAutoConfiguration engineConfiguration) {
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
            try {
                var browserCallables = engineConfiguration
                        .getBrowserCallableFinder().find(engineConfiguration);
                var processor = new ParserProcessor(engineConfiguration);
                processor.process(browserCallables);
            } catch (Exception e) {
                throw new ExecutionFailedException(
                        "Failed to generate OpenAPI spec", e);
            }
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
