package com.vaadin.hilla.internal;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.hilla.engine.EngineConfiguration;

class AbstractTaskEndpointGeneratorTest extends EndpointsTaskTest {
    @Test
    void shouldThrowIfEngineConfigurationIsNull() {
        assertThrowsExactly(NullPointerException.class, () -> {
            new TestTaskEndpointGenerator(null);
        }, "Engine configuration cannot be null");
    }

    private class TestTaskEndpointGenerator
            extends AbstractTaskEndpointGenerator {
        TestTaskEndpointGenerator(EngineConfiguration engineConfiguration) {
            super(engineConfiguration);
        }

        @Override
        public void execute() throws ExecutionFailedException {
            // no-op
        }
    }
}
