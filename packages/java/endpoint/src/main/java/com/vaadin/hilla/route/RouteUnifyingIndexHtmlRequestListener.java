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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.MenuAccessControl;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.server.auth.ViewAccessChecker;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import com.vaadin.flow.server.menu.MenuRegistry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.flow.server.menu.RouteParamType;

/**
 * Index HTML request listener for collecting the client side and the server
 * side views and adding them to index.html response.
 */
public class RouteUnifyingIndexHtmlRequestListener
        implements IndexHtmlRequestListener {
    protected static final String SCRIPT_STRING = """
            window.Vaadin = window.Vaadin ?? {};
            window.Vaadin.views = %s;""";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteUnifyingIndexHtmlRequestListener.class);
    private final NavigationAccessControl accessControl;
    private final DeploymentConfiguration deploymentConfiguration;
    private final boolean exposeServerRoutesToClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ViewAccessChecker viewAccessChecker;

    /**
     * Creates a new listener instance with the given route registry.
     *
     * @param deploymentConfiguration
     *            the runtime deployment configuration
     * @param exposeServerRoutesToClient
     *            whether to expose server routes to the client
     */
    public RouteUnifyingIndexHtmlRequestListener(
            DeploymentConfiguration deploymentConfiguration,
            @Nullable NavigationAccessControl accessControl,
            @Nullable ViewAccessChecker viewAccessChecker,
            boolean exposeServerRoutesToClient) {
        this.deploymentConfiguration = deploymentConfiguration;
        this.accessControl = accessControl;
        this.viewAccessChecker = viewAccessChecker;
        this.exposeServerRoutesToClient = exposeServerRoutesToClient;

        mapper.addMixIn(AvailableViewInfo.class, IgnoreMixin.class);
    }

    @Override
    public void modifyIndexHtmlResponse(IndexHtmlResponse response) {
        final Map<String, AvailableViewInfo> availableViews = new HashMap<>(
                collectClientViews(response.getVaadinRequest()));
        if (exposeServerRoutesToClient) {
            LOGGER.debug(
                    "Exposing server-side views to the client based on user configuration");
            availableViews
                    .putAll(collectServerViews(hasMainMenu(availableViews)));
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
            VaadinRequest request) {

        return MenuRegistry
                .collectClientMenuItems(true, deploymentConfiguration, request)
                .entrySet().stream()
                .filter(view -> !hasRequiredParameter(
                        view.getValue().routeParameters()))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    private boolean hasRequiredParameter(
            Map<String, RouteParamType> routeParameters) {
        return routeParameters != null && !routeParameters.isEmpty()
                && routeParameters.values().stream().anyMatch(
                        paramType -> paramType == RouteParamType.REQUIRED);
    }

    protected Map<String, AvailableViewInfo> collectServerViews(
            boolean hasMainMenu) {
        final var vaadinService = VaadinService.getCurrent();
        if (vaadinService == null) {
            LOGGER.debug(
                    "No VaadinService found, skipping server view collection");
            return Collections.emptyMap();
        }
        final var serverRouteRegistry = vaadinService.getRouter().getRegistry();

        var accessControls = Stream.of(accessControl, viewAccessChecker)
                .filter(Objects::nonNull).toList();

        var serverRoutes = new HashMap<String, AvailableViewInfo>();

        if (vaadinService.getInstantiator().getMenuAccessControl()
                .getPopulateClientSideMenu() == MenuAccessControl.PopulateClientMenu.ALWAYS
                || hasMainMenu) {
            MenuRegistry.collectAndAddServerMenuItems(
                    RouteConfiguration.forRegistry(serverRouteRegistry),
                    accessControls, serverRoutes);
        }

        return serverRoutes.values().stream()
                .filter(view -> view.routeParameters().values().stream()
                        .noneMatch(param -> param == RouteParamType.REQUIRED))
                .collect(Collectors.toMap(this::getMenuLink,
                        Function.identity()));
    }

    private boolean hasMainMenu(Map<String, AvailableViewInfo> availableViews) {
        Map<String, AvailableViewInfo> clientItems = new HashMap<>(
                availableViews);

        Set<String> clientEntries = new HashSet<>(clientItems.keySet());
        for (String key : clientEntries) {
            if (!clientItems.containsKey(key)) {
                continue;
            }
            AvailableViewInfo viewInfo = clientItems.get(key);
            if (viewInfo.children() != null) {
                RouteUtil.removeChildren(clientItems, viewInfo, key);
            }
        }
        return !clientItems.isEmpty() && clientItems.size() == 1
                && clientItems.values().iterator().next().route().equals("");
    }

    /**
     * Gets menu link with omitted route parameters.
     *
     * @param info
     *            the menu item's target view
     * @return target path for menu link
     */
    private String getMenuLink(AvailableViewInfo info) {
        final var parameterNames = info.routeParameters().keySet();
        return Stream.of(info.route().split("/"))
                .filter(Predicate.not(parameterNames::contains))
                .collect(Collectors.joining("/"));
    }

    /**
     * Mixin to ignore unwanted fields in the json results.
     */
    abstract static class IgnoreMixin {
        @JsonIgnore
        abstract List<AvailableViewInfo> children(); // we don't need it!
    }
}
