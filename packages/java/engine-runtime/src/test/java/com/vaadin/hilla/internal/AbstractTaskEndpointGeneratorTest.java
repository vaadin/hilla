package com.vaadin.hilla.internal;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.hilla.engine.EngineConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class AbstractTaskEndpointGeneratorTest extends TaskTest {
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
