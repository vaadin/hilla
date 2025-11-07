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
package com.vaadin.hilla.engine;

import com.vaadin.hilla.engine.commandrunner.CommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

final class GeneratorShellRunner implements CommandRunner {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GeneratorShellRunner.class);

    private final File rootDirectory;
    private final String nodeCommand;
    private final String[] arguments;

    public GeneratorShellRunner(File rootDirectory, String nodeCommand,
            String... arguments) {
        this.rootDirectory = rootDirectory;
        this.nodeCommand = nodeCommand;
        this.arguments = arguments;
    }

    @Override
    public String[] testArguments() {
        return new String[] { "-v" };
    }

    @Override
    public String[] arguments() {
        return arguments;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public File currentDirectory() {
        return rootDirectory;
    }

    @Override
    public List<String> executables() {
        return nodeCommand == null ? List.of("node")
                : List.of(nodeCommand, "node");
    }

    @Override
    public Map<String, String> environment() {
        return Map.of();
    }
}
