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
package dev.hilla.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import dev.hilla.engine.EngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.FallibleCommand;

/**
 * Abstract class for endpoint related generators.
 */
abstract class AbstractTaskEndpointGenerator implements FallibleCommand {
    private final File projectDirectory;
    private final String buildDirectoryName;
    private final File outputDirectory;
    private EngineConfiguration engineConfiguration;
    static final boolean IS_WINDOWS;
    static final String MAVEN_COMMAND;

    static {
        var osName = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = osName.contains("windows");
        MAVEN_COMMAND = IS_WINDOWS ? "mvn.cmd" : "mvn";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

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
        EngineConfiguration config = null;

        var buildDir = new File(projectDirectory, buildDirectoryName);
        try {
            config = EngineConfiguration.load(buildDir);
        } catch (IOException e) {
            logger.warn(
                    "Hilla engine configuration found, but not read correctly",
                    e);
        }

        if (config == null) {
            logger.info(
                    "Hilla engine configuration not found: configure using build system plugin");
            var command = prepareCommand();
            runConfigure(command);

            try {
                config = EngineConfiguration.load(buildDir);
            } catch (IOException e) {
                throw new ExecutionFailedException(
                        "Failed to read Hilla engine configuration", e);
            }
        }

        if (config != null) {
            config.setOutputDir(outputDirectory.toPath());
        }

        this.engineConfiguration = config;
    }

    private boolean isMavenProject(Path path) {
        return path.resolve("pom.xml").toFile().exists();
    }

    private boolean isGradleProject(Path path) {
        return path.resolve("build.gradle").toFile().exists();
    }

    List<String> prepareCommand() {
        if (projectDirectory.isDirectory()) {
            var path = projectDirectory.toPath();
            if (isMavenProject(path)) {
                return prepareMavenCommand();
            } else if (isGradleProject(path)) {
                return prepareGradleCommand();
            }
        }
        throw new IllegalStateException(String
                .format("Failed to determine project directory for dev mode. "
                        + "Directory '%s' does not look like a Maven or "
                        + "Gradle project.", projectDirectory));
    }

    List<String> prepareMavenCommand() {
        return List.of(MAVEN_COMMAND, "-q", "hilla:configure");
    }

    List<String> prepareGradleCommand() {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }

    void runConfigure(List<String> command) throws ExecutionFailedException {
        var exitCode = 0;
        try {
            ProcessBuilder builder = new ProcessBuilder(command)
                    .directory(projectDirectory).inheritIO();
            exitCode = builder.start().waitFor();
        } catch (Exception e) {
            throw new ExecutionFailedException(
                    "Emitting Hilla engine configuration failed", e);
        }
        if (exitCode != 0) {
            throw new ExecutionFailedException(
                    "Emitting Hilla engine configuration failed with exit code"
                            + exitCode);
        }
    }
}
