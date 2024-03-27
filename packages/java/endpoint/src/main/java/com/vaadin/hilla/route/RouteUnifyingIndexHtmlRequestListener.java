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
package com.vaadin.hilla.route;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.function.DeploymentConfiguration;
import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.hilla.route.records.AvailableViewInfo;
import com.vaadin.hilla.route.records.RouteParamType;

/**
 * Index HTML request listener for collecting the client side and the server
 * side views and adding them to index.html response.
 */
public class RouteUnifyingIndexHtmlRequestListener
        implements IndexHtmlRequestListener {
    protected static final String SCRIPT_STRING = """
            window.Vaadin = window.Vaadin ?? {};
            window.Vaadin.server = window.Vaadin.server ?? {};
            window.Vaadin.server.views = %s;""";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteUnifyingIndexHtmlRequestListener.class);
    private final ClientRouteRegistry clientRouteRegistry;
    private final ObjectMapper mapper = new ObjectMapper();
    private final DeploymentConfiguration deploymentConfiguration;

    private LocalDateTime lastUpdated;

    /**
     * Creates a new listener instance with the given route registry.
     *
     * @param clientRouteRegistry
     *            the client route registry for getting the client side views
     * @param deploymentConfiguration
     *            the runtime deployment configuration
     */
    public RouteUnifyingIndexHtmlRequestListener(
            ClientRouteRegistry clientRouteRegistry,
            DeploymentConfiguration deploymentConfiguration) {
        this.clientRouteRegistry = clientRouteRegistry;
        this.deploymentConfiguration = deploymentConfiguration;
    }

    @Override
    public void modifyIndexHtmlResponse(IndexHtmlResponse response) {
        final Map<String, AvailableViewInfo> availableViews = new HashMap<>();
        collectClientViews(availableViews);
        collectServerViews(availableViews);

        if (availableViews.isEmpty()) {
            return;
        }
        try {
            final String viewsJson = mapper.writeValueAsString(availableViews);
            final String script = SCRIPT_STRING.formatted(viewsJson);
            response.getDocument().head().appendElement("script")
                    .appendChild(new DataNode(script));
        } catch (IOException e) {
            LOGGER.error("Failed to write server views to index response", e);
        }
    }

    protected void collectClientViews(
            Map<String, AvailableViewInfo> availableViews) {
        if (!deploymentConfiguration.isProductionMode()) {
            loadLatestDevModeViewsJsonIfNeeded();
        } else if (lastUpdated == null) {
            // initial (and only) registration in production mode:
            registerClientRoutes(LocalDateTime.now());
        }
        clientRouteRegistry.getAllRoutes().forEach((route, config) -> {
            final AvailableViewInfo availableViewInfo = new AvailableViewInfo(
                    config.getTitle(), config.getRolesAllowed(),
                    config.isLoginRequired(), config.getRoute(),
                    config.isLazy(), config.isAutoRegistered(), config.menu(),
                    config.getRouteParameters());
            availableViews.put(route, availableViewInfo);
        });

    }

    private void loadLatestDevModeViewsJsonIfNeeded() {
        var devModeViewsJsonFile = deploymentConfiguration.getFrontendFolder()
                .toPath().resolve("generated").resolve("views.json").toFile();
        if (!devModeViewsJsonFile.exists()) {
            LOGGER.warn("Failed to find views.json under {}",
                    deploymentConfiguration.getFrontendFolder().toPath()
                            .resolve("generated"));
            return;
        }
        var lastModified = devModeViewsJsonFile.lastModified();
        var lastModifiedTime = Instant.ofEpochMilli(lastModified)
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (lastUpdated == null || lastModifiedTime.isAfter(lastUpdated)) {
            registerClientRoutes(lastModifiedTime);
        }
    }

    private void registerClientRoutes(LocalDateTime newLastUpdated) {
        lastUpdated = newLastUpdated;
        clientRouteRegistry.registerClientRoutes(deploymentConfiguration);
    }

    protected void collectServerViews(
            final Map<String, AvailableViewInfo> serverViews) {
        final VaadinService vaadinService = VaadinService.getCurrent();
        if (vaadinService == null) {
            LOGGER.debug(
                    "No VaadinService found, skipping server view collection");
            return;
        }
        final RouteRegistry serverRouteRegistry = vaadinService.getRouter()
                .getRegistry();
        serverRouteRegistry.getRegisteredRoutes().forEach(serverView -> {
            final Class<? extends com.vaadin.flow.component.Component> viewClass = serverView
                    .getNavigationTarget();
            final String targetUrl = serverView.getTemplate();
            if (targetUrl != null) {
                final String url = "/" + targetUrl;

                final String title;
                PageTitle pageTitle = viewClass.getAnnotation(PageTitle.class);
                if (pageTitle != null) {
                    title = pageTitle.value();
                } else {
                    title = serverView.getNavigationTarget().getSimpleName();
                }

                final Map<String, RouteParamType> routeParameters = getRouteParameters(
                        serverView);

                final AvailableViewInfo availableViewInfo = new AvailableViewInfo(
                        title, null, false, url, false, false, null,
                        routeParameters);
                serverViews.put(url, availableViewInfo);
            }
        });
    }

    private Map<String, RouteParamType> getRouteParameters(
            RouteData serverView) {
        final Map<String, RouteParamType> routeParameters = new HashMap<>();
        serverView.getRouteParameters().forEach((route, params) -> {
            if (params.getTemplate().contains("*")) {
                routeParameters.put(params.getTemplate(),
                        RouteParamType.WILDCARD);
            } else if (params.getTemplate().contains("?")) {
                routeParameters.put(params.getTemplate(),
                        RouteParamType.OPTIONAL);
            } else {
                routeParameters.put(params.getTemplate(),
                        RouteParamType.REQUIRED);
            }
        });
        return routeParameters;
    }
}
