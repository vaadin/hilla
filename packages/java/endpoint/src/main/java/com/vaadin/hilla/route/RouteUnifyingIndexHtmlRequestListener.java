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
import java.util.function.Predicate;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.hilla.route.records.ClientViewConfig;
import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.hilla.route.records.AvailableViewInfo;
import com.vaadin.hilla.route.records.ClientViewMenuConfig;
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
    private final RouteUtil routeUtil;
    private final boolean exposeServerRoutesToClient;

    private LocalDateTime lastUpdated;

    /**
     * Creates a new listener instance with the given route registry.
     *
     * @param clientRouteRegistry
     *            the client route registry for getting the client side views
     * @param deploymentConfiguration
     *            the runtime deployment configuration
     * @param routeUtil
     *            the ClientRouteRegistry aware utility for checking if user is
     *            allowed to access a route
     * @param exposeServerRoutesToClient
     *            whether to expose server routes to the client
     */
    public RouteUnifyingIndexHtmlRequestListener(
            ClientRouteRegistry clientRouteRegistry,
            DeploymentConfiguration deploymentConfiguration,
            RouteUtil routeUtil, boolean exposeServerRoutesToClient) {
        this.clientRouteRegistry = clientRouteRegistry;
        this.deploymentConfiguration = deploymentConfiguration;
        this.routeUtil = routeUtil;
        this.exposeServerRoutesToClient = exposeServerRoutesToClient;
    }

    @Override
    public void modifyIndexHtmlResponse(IndexHtmlResponse response) {
        final boolean isUserAuthenticated = response.getVaadinRequest()
                .getUserPrincipal() != null;
        final Map<String, AvailableViewInfo> availableViews = new HashMap<>(
                collectClientViews(response.getVaadinRequest()::isUserInRole,
                        isUserAuthenticated));
        if (exposeServerRoutesToClient) {
            LOGGER.debug(
                    "Exposing server-side views to the client based on user configuration");
            availableViews.putAll(collectServerViews());
        }

        if (availableViews.isEmpty()) {
            LOGGER.debug(
                    "No server-side nor client-side views found, skipping response modification.");
            return;
        }
        try {
            final String fileRoutesJson = mapper
                    .writeValueAsString(availableViews);
            final String script = SCRIPT_STRING.formatted(fileRoutesJson);
            response.getDocument().head().appendElement("script")
                    .appendChild(new DataNode(script));
        } catch (IOException e) {
            LOGGER.error(
                    "Failure while to write client and server routes to index html response",
                    e);
        }
    }

    protected Map<String, AvailableViewInfo> collectClientViews(
            Predicate<? super String> isUserInRole,
            boolean isUserAuthenticated) {
        if (!deploymentConfiguration.isProductionMode()) {
            loadLatestDevModeFileRoutesJsonIfNeeded();
        } else if (lastUpdated == null) {
            // initial (and only) registration in production mode:
            registerClientRoutes(LocalDateTime.now());
        }

        var clientViews = new HashMap<String, AvailableViewInfo>();
        clientRouteRegistry.getAllRoutes().entrySet().stream()
                .filter(clientViewConfigEntry -> routeUtil.isRouteAllowed(
                        isUserInRole, isUserAuthenticated,
                        clientViewConfigEntry.getValue()))
                .forEach(clientViewConfigEntry -> {
                    final String route = clientViewConfigEntry.getKey();
                    final ClientViewConfig config = clientViewConfigEntry
                            .getValue();
                    final AvailableViewInfo availableViewInfo = new AvailableViewInfo(
                            config.getTitle(), config.getRolesAllowed(),
                            config.isLoginRequired(), config.getRoute(),
                            config.isLazy(), config.isAutoRegistered(),
                            config.menu(), config.getRouteParameters());
                    clientViews.put(route, availableViewInfo);
                });
        return clientViews;
    }

    private void loadLatestDevModeFileRoutesJsonIfNeeded() {
        var devModeFileRoutesJsonFile = deploymentConfiguration
                .getFrontendFolder().toPath().resolve("generated")
                .resolve("file-routes.json").toFile();
        if (!devModeFileRoutesJsonFile.exists()) {
            LOGGER.debug("No file-routes.json found under {}",
                    deploymentConfiguration.getFrontendFolder().toPath()
                            .resolve("generated"));
            return;
        }
        var lastModified = devModeFileRoutesJsonFile.lastModified();
        var lastModifiedTime = Instant.ofEpochMilli(lastModified)
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (lastUpdated == null || lastModifiedTime.isAfter(lastUpdated)) {
            registerClientRoutes(lastModifiedTime);
        }
    }

    private void registerClientRoutes(LocalDateTime newLastUpdated) {
        var hasClientRoutesRegistered = clientRouteRegistry
                .registerClientRoutes(deploymentConfiguration);
        if (hasClientRoutesRegistered) {
            lastUpdated = newLastUpdated;
        }
    }

    protected Map<String, AvailableViewInfo> collectServerViews() {
        var serverViews = new HashMap<String, AvailableViewInfo>();
        final VaadinService vaadinService = VaadinService.getCurrent();
        if (vaadinService == null) {
            LOGGER.debug(
                    "No VaadinService found, skipping server view collection");
            return serverViews;
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
                PageTitle pageTitle = AnnotationReader
                        .getAnnotationFor(viewClass, PageTitle.class)
                        .orElse(null);
                if (pageTitle != null) {
                    title = pageTitle.value();
                } else {
                    title = serverView.getNavigationTarget().getSimpleName();
                }

                final ClientViewMenuConfig menuConfig = AnnotationReader
                        .getAnnotationFor(viewClass, Menu.class)
                        .map(menu -> new ClientViewMenuConfig(
                                menu.title().isBlank() ? title : menu.title(),
                                (menu.order() == Long.MIN_VALUE) ? null
                                        : menu.order(),
                                menu.icon(), menu.exclude()))
                        .orElse(null);

                final Map<String, RouteParamType> routeParameters = getRouteParameters(
                        serverView);

                final AvailableViewInfo availableViewInfo = new AvailableViewInfo(
                        title, null, false, url, false, false, menuConfig,
                        routeParameters);
                serverViews.put(url, availableViewInfo);
            }
        });
        return serverViews;
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
