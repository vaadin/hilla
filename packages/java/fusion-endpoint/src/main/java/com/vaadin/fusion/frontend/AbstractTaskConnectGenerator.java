/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.frontend.FallibleCommand;

/**
 * Abstract class for Vaadin Fusion related generators.
 */
abstract class AbstractTaskConnectGenerator implements FallibleCommand {
    private final File applicationProperties;

    AbstractTaskConnectGenerator(File applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    protected Properties readApplicationProperties() {
        Properties config = new Properties();

        if (applicationProperties != null && applicationProperties.exists()) {
            try (BufferedReader bufferedReader = Files.newBufferedReader(
                    applicationProperties.toPath(), StandardCharsets.UTF_8)) {
                config.load(bufferedReader);
            } catch (IOException e) {
                log().info(String.format(
                        "Can't read the application"
                                + ".properties file from %s",
                        applicationProperties.toString()), e);
            }
        } else {
            log().debug(
                    "Found no application properties, using default values.");
        }
        return config;
    }

    Logger log() {
        return LoggerFactory.getLogger(AbstractTaskConnectGenerator.class);
    }
}
