/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.hilla;

import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Reports Hilla statistics. Internal.
 */
class HillaStats {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointController.class);

    private static Optional<String> getHillaReactVersion() {
        try (final InputStream pomProperties = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(
                        "META-INF/maven/com.vaadin.hilla/hilla-react/pom.properties")) {
            if (pomProperties != null) {
                final Properties properties = new Properties();
                properties.load(pomProperties);
                return Optional.of(properties.getProperty("version", ""));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.error(
                    "Unable to determine com.vaadin.hilla/hilla-react version",
                    e);
        }
        return Optional.of("?");
    }

    private static Optional<String> getHillaLitVersion() {
        // there's no hilla-lit.jar file at the moment. Therefore,
        // if Hilla-React is missing, we'll assume the Lit presence.
        final Optional<String> hillaReactVersion = getHillaReactVersion();
        return hillaReactVersion.isPresent() ? Optional.empty()
                : Platform.getHillaVersion();
    }

    public static void report() {
        UsageStatistics.markAsUsed("hilla",
                Platform.getHillaVersion().orElse("?"));
        final Optional<String> hillaReactVersion = getHillaReactVersion();
        hillaReactVersion.ifPresent(
                version -> UsageStatistics.markAsUsed("hilla+react", version));
        final Optional<String> hillaLitVersion = getHillaLitVersion();
        hillaLitVersion.ifPresent(
                version -> UsageStatistics.markAsUsed("hilla+lit", version));
    }
}
