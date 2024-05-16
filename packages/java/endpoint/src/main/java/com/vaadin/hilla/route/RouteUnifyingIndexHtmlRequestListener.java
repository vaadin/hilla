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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.MenuAccessControl;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.server.auth.ViewAccessChecker;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import com.vaadin.flow.server.menu.MenuRegistry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.Nullable;

import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;

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

    private final ClientRouteRegistry clientRouteRegistry;
    private final ObjectMapper mapper = new ObjectMapper();
    private final DeploymentConfiguration deploymentConfiguration;
    private final RouteUtil routeUtil;
    private final NavigationAccessControl accessControl;
    private final ViewAccessChecker viewAccessChecker;
    private final boolean exposeServerRoutesToClient;

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
            RouteUtil routeUtil,
            @Nullable NavigationAccessControl accessControl,
            @Nullable ViewAccessChecker viewAccessChecker,
            boolean exposeServerRoutesToClient) {
        this.clientRouteRegistry = clientRouteRegistry;
        this.deploymentConfiguration = deploymentConfiguration;
        this.routeUtil = routeUtil;
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
            VaadinRequest request) {

        if (!deploymentConfiguration.isProductionMode()) {
            clientRouteRegistry.loadLatestDevModeFileRoutesJsonIfNeeded(
                    deploymentConfiguration);
        }

        return MenuRegistry.collectClientMenuItems(true,
                deploymentConfiguration, request);
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

        List<BeforeEnterListener> accessControls = Stream
                .of(accessControl, viewAccessChecker).filter(Objects::nonNull)
                .toList();

        if (vaadinService.getInstantiator().getMenuAccessControl()
                .getPopulateClientSideMenu() == MenuAccessControl.PopulateClientMenu.ALWAYS
                || clientRouteRegistry.hasMainLayout()) {
            MenuRegistry.collectAndAddServerMenuItems(
                    RouteConfiguration.forRegistry(serverRouteRegistry),
                    accessControls, serverViews);
        }
        return serverViews;
    }

    /**
     * Mixin to ignore unwanted fields in the json results.
     */
    abstract class IgnoreMixin {
        @JsonIgnore
        abstract List<AvailableViewInfo> children(); // we don't need it!
    }
}
