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

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;
import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.GeneratorProcessor;

/**
 * Starts the generation of TS files for endpoints.
 */
public class TaskGenerateEndpointImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateEndpoint {

    /**
     * Create a task for generating OpenAPI spec.
     *
     * @param engineConfiguration
     *            Hilla engine configuration instance
     */
    TaskGenerateEndpointImpl(EngineConfiguration engineConfiguration) {
        super(engineConfiguration);
    }

    /**
     * Run TypeScript code generator.
     *
     * @throws ExecutionFailedException
     */
    @Override
    public void execute() throws ExecutionFailedException {
        if (getEngineConfiguration().isProductionMode()) {
            runProcessor();
        } else {
            // Even if we don't need the application context here, we have to
            // wait for the parser to complete its job, so we add this the
            // context queue.
            ApplicationContextProvider.runOnContext(applicationContext -> {
                runProcessor();
            });
        }
    }

    private void runProcessor() {
        var processor = new GeneratorProcessor(getEngineConfiguration());
        processor.process();
    }
}
