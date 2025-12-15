/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.engine.commandrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a Maven command.
 */
public class MavenRunner implements CommandRunner {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MavenRunner.class);

    static final String EXECUTABLE_PROPERTY = "hilla.mavenExecutable";
    private final File projectDir;
    private final String[] args;

    /**
     * Creates a Maven runner.
     *
     * @param projectDir
     *            the project directory
     * @param args
     *            the arguments to pass to Maven
     */
    public MavenRunner(File projectDir, String... args) {
        this.projectDir = projectDir;
        this.args = args;
    }

    /**
     * Creates a Maven runner for the given project directory.
     *
     * @param projectDir
     *            the project directory
     * @param args
     *            the arguments to pass to Maven
     * @return a Maven runner if the project directory contains a Maven project,
     *         an empty optional otherwise
     */
    public static Optional<CommandRunner> forProject(File projectDir,
            String... args) {
        if (new File(projectDir, "pom.xml").exists()) {
            return Optional.of(new MavenRunner(projectDir, args));
        }

        return Optional.empty();
    }

    @Override
    public String[] arguments() {
        return args;
    }

    @Override
    public String[] testArguments() {
        return new String[] { "-v" };
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public File currentDirectory() {
        return projectDir;
    }

    @Override
    public List<String> executables() {
        List<String> executableList = new ArrayList<>();
        String customExecutable = System.getProperty(EXECUTABLE_PROPERTY);
        if (customExecutable != null) {
            executableList.add(customExecutable);
        }
        // Prefer the wrapper over a global installation
        if (IS_WINDOWS) {
            executableList.add(".\\mvnw.cmd");
            executableList.add("mvn.cmd");
        } else {
            executableList.add("./mvnw");
            executableList.add("mvn");
        }
        return List.copyOf(executableList);
    }
}
