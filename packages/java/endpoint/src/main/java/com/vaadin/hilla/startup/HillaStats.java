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
package com.vaadin.hilla.startup;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.Platform;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.hilla.EndpointController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

/**
 * Reports Hilla statistics. Internal.
 */
class HillaStats {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointController.class);

    private static Optional<String> getHillaReactVersion(
            DeploymentConfiguration deploymentConfiguration) {
        var frontendFolder = deploymentConfiguration.getFrontendFolder();
        boolean isReactUsed = FrontendUtils.isHillaUsed(frontendFolder)
                && FrontendUtils.isReactRouterRequired(frontendFolder)
                && deploymentConfiguration.isReactEnabled();
        LOGGER.debug("Hilla React usage detected: {}", isReactUsed);
        return isReactUsed ? Platform.getHillaVersion() : Optional.empty();
    }

    private static Optional<String> getHillaLitVersion(
            DeploymentConfiguration deploymentConfiguration) {
        var frontendFolder = deploymentConfiguration.getFrontendFolder();
        boolean isLitUsed = FrontendUtils.isHillaUsed(frontendFolder)
                && !FrontendUtils.isReactRouterRequired(frontendFolder)
                && !deploymentConfiguration.isReactEnabled();
        LOGGER.debug("Hilla Lit usage detected: {}", isLitUsed);
        return isLitUsed ? Platform.getHillaVersion() : Optional.empty();
    }

    private static Optional<String> getHillaLitReactVersion(
            DeploymentConfiguration deploymentConfiguration) {
        var frontendFolder = deploymentConfiguration.getFrontendFolder();
        boolean isLitReactUsed = FrontendUtils.isHillaUsed(frontendFolder)
                && FrontendUtils.isReactRouterRequired(frontendFolder)
                && !deploymentConfiguration.isReactEnabled();
        LOGGER.debug("Hilla Lit+React usage detected: {}", isLitReactUsed);
        return isLitReactUsed ? Platform.getHillaVersion() : Optional.empty();
    }

    public static void report(DeploymentConfiguration deploymentConfiguration) {
        UsageStatistics.markAsUsed("hilla",
                Platform.getHillaVersion().orElse("?"));
        LOGGER.debug(
                "Hilla version determined by Platform.getHillaVersion(): {}",
                Platform.getHillaVersion().orElse("?"));
        final Optional<String> hillaReactVersion = getHillaReactVersion(
                deploymentConfiguration);
        hillaReactVersion.ifPresent(
                version -> UsageStatistics.markAsUsed("hilla+react", version));
        final Optional<String> hillaLitVersion = getHillaLitVersion(
                deploymentConfiguration);
        hillaLitVersion.ifPresent(
                version -> UsageStatistics.markAsUsed("hilla+lit", version));
        final Optional<String> hillaLitReactVersion = getHillaLitReactVersion(
                deploymentConfiguration);
        hillaLitReactVersion.ifPresent(version -> UsageStatistics
                .markAsUsed("hilla+lit+react", version));
    }
}
