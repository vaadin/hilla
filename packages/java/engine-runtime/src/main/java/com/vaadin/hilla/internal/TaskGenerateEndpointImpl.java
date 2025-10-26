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

import java.util.List;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;
import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.EndpointCodeGenerator;
import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.hilla.engine.GeneratorProcessor;

/**
 * Starts the generation of TS files for endpoints.
 */
public class TaskGenerateEndpointImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateEndpoint {

    /**
     * Create a task for generating TypeScript endpoint clients.
     *
     * @param engineConfiguration
     *            Hilla engine configuration instance
     */
    TaskGenerateEndpointImpl(EngineAutoConfiguration engineConfiguration) {
        super(engineConfiguration);
    }

    /**
     * Run TypeScript code generator.
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
                runProcessor(browserCallables);
            } catch (Exception e) {
                throw new ExecutionFailedException(
                        "Failed to generate TypeScript files", e);
            }
        } else {
            ApplicationContextProvider.runOnContext(applicationContext -> {
                List<Class<?>> browserCallables = EndpointCodeGenerator
                        .findBrowserCallables(engineConfiguration,
                                applicationContext);
                runProcessor(browserCallables);
            });
        }
    }

    private void runProcessor(List<Class<?>> browserCallables) {
        var processor = new GeneratorProcessor(getEngineConfiguration());
        processor.process(browserCallables);
    }
}
