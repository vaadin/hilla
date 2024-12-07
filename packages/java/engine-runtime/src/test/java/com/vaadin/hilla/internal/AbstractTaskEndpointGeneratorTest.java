package com.vaadin.hilla.internal;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Function;

import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.commandrunner.CommandRunnerException;
import com.vaadin.hilla.engine.commandrunner.MavenRunner;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.ExecutionFailedException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class AbstractTaskEndpointGeneratorTest extends TaskTest {
    @Test
    void shouldThrowIfEngineConfigurationIsNull() {
        assertThrowsExactly(NullPointerException.class, () -> {
            new TestTaskEndpointGenerator(null);
        }, "Engine configuration cannot be null");
    }

    private final Function<String, URL> resourceFinder = Thread.currentThread()
            .getContextClassLoader()::getResource;

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
