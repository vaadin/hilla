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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads Hilla feature toggle properties from
 * {@code application.properties}. All features default to {@code true}.
 * <p>
 * Supported properties:
 * <ul>
 * <li>{@code hilla.file-router.enabled} &ndash; controls file-based
 * routing Spring beans and generated route code</li>
 * <li>{@code hilla.auto-crud.enabled} &ndash; controls CRUD model
 * generation and CRUD infrastructure</li>
 * <li>{@code hilla.vaadin-ui.enabled} &ndash; controls Vaadin
 * UI&ndash;specific code generation (form models)</li>
 * </ul>
 */
public final class HillaFeatureProperties {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HillaFeatureProperties.class);

    private static final String PROP_FILE_ROUTER = "hilla.file-router.enabled";
    private static final String PROP_AUTO_CRUD = "hilla.auto-crud.enabled";
    private static final String PROP_VAADIN_UI = "hilla.vaadin-ui.enabled";

    private final boolean fileRouterEnabled;
    private final boolean autoCrudEnabled;
    private final boolean vaadinUiEnabled;

    /**
     * Creates an instance with the given feature flags.
     */
    public HillaFeatureProperties(boolean fileRouterEnabled,
            boolean autoCrudEnabled, boolean vaadinUiEnabled) {
        this.fileRouterEnabled = fileRouterEnabled;
        this.autoCrudEnabled = autoCrudEnabled;
        this.vaadinUiEnabled = vaadinUiEnabled;
    }

    /**
     * Returns a default instance with all features enabled.
     */
    public static HillaFeatureProperties defaults() {
        return new HillaFeatureProperties(true, true, true);
    }

    /**
     * Reads feature properties from {@code application.properties} located
     * under the given base directory (typically
     * {@code src/main/resources/application.properties}).
     * <p>
     * If the file does not exist or cannot be read, all features default to
     * {@code true}.
     *
     * @param baseDir
     *            the project base directory
     * @return the parsed feature properties
     */
    public static HillaFeatureProperties fromBaseDir(Path baseDir) {
        Path propsFile = baseDir.resolve("src/main/resources/application.properties");
        if (!Files.exists(propsFile)) {
            LOGGER.debug(
                    "No application.properties found at {}, all Hilla features enabled by default.",
                    propsFile);
            return defaults();
        }

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(propsFile)) {
            props.load(is);
        } catch (IOException e) {
            LOGGER.warn(
                    "Failed to read application.properties at {}, all Hilla features enabled by default.",
                    propsFile, e);
            return defaults();
        }

        boolean fileRouter = getBooleanProperty(props, PROP_FILE_ROUTER, true);
        boolean autoCrud = getBooleanProperty(props, PROP_AUTO_CRUD, true);
        boolean vaadinUi = getBooleanProperty(props, PROP_VAADIN_UI, true);

        LOGGER.debug(
                "Hilla feature properties: file-router={}, auto-crud={}, vaadin-ui={}",
                fileRouter, autoCrud, vaadinUi);

        return new HillaFeatureProperties(fileRouter, autoCrud, vaadinUi);
    }

    /**
     * Whether file-based routing is enabled.
     */
    public boolean isFileRouterEnabled() {
        return fileRouterEnabled;
    }

    /**
     * Whether auto CRUD generation is enabled.
     */
    public boolean isAutoCrudEnabled() {
        return autoCrudEnabled;
    }

    /**
     * Whether Vaadin UI-specific code generation is enabled.
     */
    public boolean isVaadinUiEnabled() {
        return vaadinUiEnabled;
    }

    private static boolean getBooleanProperty(Properties props, String key,
            boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
