/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
