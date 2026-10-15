/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.internal;

import java.util.Objects;

import com.vaadin.flow.server.frontend.FallibleCommand;
import com.vaadin.hilla.engine.EngineConfiguration;

/**
 * Abstract class for endpoint related generators.
 */
abstract class AbstractTaskEndpointGenerator implements FallibleCommand {
    private final EngineConfiguration engineConfiguration;

    AbstractTaskEndpointGenerator(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = Objects.requireNonNull(engineConfiguration,
                "Engine configuration cannot be null");
    }

    protected EngineConfiguration getEngineConfiguration() {
        return engineConfiguration;
    }
}
