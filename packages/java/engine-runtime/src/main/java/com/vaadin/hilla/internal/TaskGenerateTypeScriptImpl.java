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

import com.vaadin.flow.server.frontend.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;
import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.EndpointCodeGenerator;
import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.hilla.engine.ParserProcessor;
import com.vaadin.hilla.engine.TypeScriptProcessor;

/**
 * Writes the TypeScript the browser calls the endpoints of the application
 * through.
 *
 * <p>
 * It is the {@link TaskGenerateOpenAPI} of the build because that is the task
 * which runs while the browser callable classes are walked, which is when the
 * writers have what they need. The task of the endpoints is left with nothing
 * to do.
 */
public class TaskGenerateTypeScriptImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateOpenAPI {

    /**
     * Create a task for writing the TypeScript of the endpoints.
     *
     * @param engineConfiguration
     *            Hilla engine configuration instance
     */
    TaskGenerateTypeScriptImpl(EngineAutoConfiguration engineConfiguration) {
        super(engineConfiguration);
    }

    /**
     * Walks the browser callable classes and writes the TypeScript of them.
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
                write(engineConfiguration, browserCallables);
            } catch (Exception e) {
                throw new ExecutionFailedException(
                        "Failed to write the TypeScript of the endpoints", e);
            }
        } else {
            ApplicationContextProvider.runOnContext(applicationContext -> {
                List<Class<?>> browserCallables = EndpointCodeGenerator
                        .findBrowserCallables(engineConfiguration,
                                applicationContext);
                write(engineConfiguration, browserCallables);
            });
        }
    }

    /**
     * Runs the parser over the browser callable classes and writes the
     * TypeScript of the endpoints out of what it found.
     */
    private static void write(EngineAutoConfiguration engineConfiguration,
            List<Class<?>> browserCallables) {
        var generation = new ParserProcessor(engineConfiguration)
                .parse(browserCallables);

        new TypeScriptProcessor(engineConfiguration).process(generation);
    }
}
