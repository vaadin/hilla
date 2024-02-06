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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.Platform;
import com.vaadin.flow.server.frontend.FallibleCommand;

import com.vaadin.hilla.engine.ConfigurationException;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.commandrunner.GradleRunner;
import com.vaadin.hilla.engine.commandrunner.MavenRunner;
import com.vaadin.hilla.engine.commandrunner.CommandRunnerException;

/**
 * Abstract class for endpoint related generators.
 */
abstract class AbstractTaskEndpointGenerator implements FallibleCommand {
    private static boolean firstRun = true;

    private final String buildDirectoryName;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final File outputDirectory;
    private final File projectDirectory;
    private EngineConfiguration engineConfiguration;

    AbstractTaskEndpointGenerator(File projectDirectory,
            String buildDirectoryName, File outputDirectory) {
        this.projectDirectory = Objects.requireNonNull(projectDirectory,
                "Project directory cannot be null");
        this.buildDirectoryName = Objects.requireNonNull(buildDirectoryName,
                "Build directory name cannot be null");
        this.outputDirectory = Objects.requireNonNull(outputDirectory,
                "Output direrctory name cannot be null");
    }

    protected EngineConfiguration getEngineConfiguration()
            throws ExecutionFailedException {
        if (engineConfiguration == null) {
            prepareEngineConfiguration();
        }

        return engineConfiguration;
    }

    protected void prepareEngineConfiguration()
            throws ExecutionFailedException {
        var configDir = projectDirectory.toPath().resolve(buildDirectoryName);

        if (firstRun) {
            logger.debug("Configure Hilla engine using build system plugin");

            var mavenConfigure = MavenRunner.forProject(projectDirectory, "-q",
                    "vaadin:configure");
            var mavenConfigureVersion = Platform.getVaadinVersion()
                    .flatMap(version -> MavenRunner.forProject(projectDirectory,
                            "-q", "com.vaadin:vaadin-maven-plugin:" + version
                                    + ":configure"));
            var gradleConfigure = GradleRunner.forProject(projectDirectory,
                    "-q", "hillaConfigure");

            var runners = Stream
                    .of(mavenConfigure, mavenConfigureVersion, gradleConfigure)
                    .flatMap(Optional::stream).toList();

            if (runners.isEmpty()) {
                throw new ExecutionFailedException(String.format(
                        "Failed to determine project directory for dev mode. "
                                + "Directory '%s' does not look like a Maven or "
                                + "Gradle project.",
                        projectDirectory));
            } else {
                for (var runner : runners) {
                    try {
                        runner.run(null);
                        firstRun = false;
                        break;
                    } catch (CommandRunnerException e) {
                        logger.debug(
                                "Failed to configure Hilla engine using "
                                        + runner.getClass().getSimpleName()
                                        + " with arguments "
                                        + Arrays.toString(runner.arguments()),
                                e);
                    }
                }
            }

            if (firstRun) {
                throw new ExecutionFailedException(
                        "Failed to configure Hilla engine: no runner succeeded. "
                                + "Set log level to debug to see more details.");
            }
        }

        try {
            var config = EngineConfiguration.loadDirectory(configDir);

            if (config == null) {
                throw new ExecutionFailedException(
                        "Engine configuration is missing");
            }

            this.engineConfiguration = new EngineConfiguration.Builder(config)
                    .outputDir(outputDirectory.toPath()).create();
        } catch (IOException | ConfigurationException e) {
            throw new ExecutionFailedException(
                    "Failed to read Hilla engine configuration", e);
        }

    }
}
