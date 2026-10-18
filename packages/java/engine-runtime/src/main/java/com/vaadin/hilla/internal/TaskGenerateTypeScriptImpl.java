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

import java.util.Objects;

import com.vaadin.flow.server.frontend.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;
import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.EndpointCodeGenerator;
import com.vaadin.hilla.engine.EngineAutoConfiguration;
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
public class TaskGenerateTypeScriptImpl implements TaskGenerateOpenAPI {
    private final EngineAutoConfiguration engineConfiguration;

    /**
     * Create a task for writing the TypeScript of the endpoints.
     *
     * @param engineConfiguration
     *            Hilla engine configuration instance
     */
    TaskGenerateTypeScriptImpl(EngineAutoConfiguration engineConfiguration) {
        this.engineConfiguration = Objects.requireNonNull(engineConfiguration,
                "Engine configuration cannot be null");
    }

    /**
     * Walks the browser callable classes and writes the TypeScript of them.
     *
     * @throws ExecutionFailedException
     */
    @Override
    public void execute() throws ExecutionFailedException {
        if (engineConfiguration.isProductionMode()) {
            try {
                TypeScriptProcessor.generate(engineConfiguration);
            } catch (Exception e) {
                throw new ExecutionFailedException(
                        "Failed to write the TypeScript of the endpoints", e);
            }
        } else {
            // The generator of the running application is told what the
            // TypeScript is written from, which spares it a walk of its own
            // on the first change to a class
            ApplicationContextProvider.runOnContext(applicationContext -> {
                var codeGenerator = applicationContext
                        .getBeanProvider(EndpointCodeGenerator.class)
                        .getIfAvailable();

                if (codeGenerator != null) {
                    codeGenerator.generate(engineConfiguration,
                            applicationContext);
                } else {
                    TypeScriptProcessor.generate(engineConfiguration,
                            EndpointCodeGenerator.findBrowserCallables(
                                    engineConfiguration, applicationContext));
                }
            });
        }
    }
}
