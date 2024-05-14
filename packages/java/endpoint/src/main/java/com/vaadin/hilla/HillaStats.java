/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.Platform;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.frontend.FrontendUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;

/**
 * Reports Hilla statistics. Internal.
 */
public class HillaStats {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointController.class);

    static final String HAS_REACT = "has-react";
    static final String HAS_LIT = "has-lit";
    static final String HAS_REACT_LIT = "has-react-lit";
    static final String HAS_HILLA_FS_ROUTE = "has-hilla-fs-route";
    static final String HAS_HILLA_CUSTOM_ROUTE = "has-hilla-custom-route";
    static final String HAS_HYBRID_ROUTING = "has-hybrid-routing";
    static final String HAS_ENDPOINT = "has-endpoint";
    static final String ENDPOINT_ACTIVE = "endpoint-active";
    static final String HILLA_USAGE = "hilla";

    private static void reportHasReactAndLit(
            DeploymentConfiguration deploymentConfiguration,
            String hillaVersion) {
        try {
            var frontendFolder = deploymentConfiguration.getFrontendFolder();
            var isHillaUsed = FrontendUtils.isHillaUsed(frontendFolder);
            var isReactRouterRequired = FrontendUtils
                    .isReactRouterRequired(frontendFolder);

            if (isHillaUsed && isReactRouterRequired
                    && deploymentConfiguration.isReactEnabled()) {
                UsageStatistics.markAsUsed(HAS_REACT, hillaVersion);
            }

            if (isHillaUsed && !isReactRouterRequired
                    && !deploymentConfiguration.isReactEnabled()) {
                UsageStatistics.markAsUsed(HAS_LIT, hillaVersion);
            }

            if (isHillaUsed && isReactRouterRequired
                    && !deploymentConfiguration.isReactEnabled()) {
                UsageStatistics.markAsUsed(HAS_REACT_LIT, hillaVersion);
            }
        } catch (Throwable e) {
            LOGGER.debug("Failed to report HasReactAndLit", e);
        }
    }

    private static void reportHasRouter(VaadinService service,
            boolean hasHillaFsRoute, String hillaVersion) {
        try {
            if (hasHillaFsRoute) {
                UsageStatistics.markAsUsed(HAS_HILLA_FS_ROUTE, hillaVersion);
            }

            var deploymentConfiguration = service.getDeploymentConfiguration();
            var frontendFolderPath = deploymentConfiguration.getFrontendFolder()
                    .toPath();
            var routesTsx = frontendFolderPath.resolve("routes.tsx");
            var routesTs = frontendFolderPath.resolve("routes.ts");
            var hasHillaCustomRoute = Files.exists(routesTsx)
                    || Files.exists(routesTs);
            if (hasHillaCustomRoute) {
                UsageStatistics.markAsUsed(HAS_HILLA_CUSTOM_ROUTE,
                        hillaVersion);
            }

            if (hasHillaFsRoute || hasHillaCustomRoute) {
                UsageStatistics.markAsUsed(HILLA_USAGE, hillaVersion);
            }

            var hasFlowRoute = !service.getRouter().getRegistry()
                    .getRegisteredRoutes().isEmpty();

            var hasHybridRouting = hasFlowRoute
                    && (hasHillaFsRoute || hasHillaCustomRoute);
            if (hasHybridRouting) {
                UsageStatistics.markAsUsed(HAS_HYBRID_ROUTING, hillaVersion);
            }
        } catch (Throwable e) {
            LOGGER.debug("Failed to report HasRouter", e);
        }
    }

    private static String getHillaVersion() {
        var hillaVersion = Platform.getHillaVersion().orElse("?");
        LOGGER.debug(
                "Hilla version determined by Platform.getHillaVersion(): {}",
                hillaVersion);
        return hillaVersion;
    }

    public static void reportGenericHasFeatures(VaadinService service,
            boolean hasHillaFsRoute) {
        var deploymentConfiguration = service.getDeploymentConfiguration();
        var hillaVersion = getHillaVersion();
        reportHasReactAndLit(deploymentConfiguration, hillaVersion);
        reportHasRouter(service, hasHillaFsRoute, hillaVersion);
    }

    public static void reportHasEndpoint() {
        try {
            UsageStatistics.markAsUsed(HAS_ENDPOINT, getHillaVersion());
        } catch (Throwable e) {
            LOGGER.debug("Failed to report Hilla statistics", e);
        }
    }

    public static void reportEndpointActive() {
        try {
            UsageStatistics.markAsUsed(ENDPOINT_ACTIVE, getHillaVersion());
        } catch (Throwable e) {
            LOGGER.debug("Failed to report Hilla statistics", e);
        }
    }
}
