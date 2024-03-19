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
import java.util.Map;

import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class RouteExtractionIndexHtmlRequestListener
        implements IndexHtmlRequestListener {
    protected static final String SCRIPT_STRING = """
            window.Vaadin = window.Vaadin ?? {};
            window.Vaadin.server = window.Vaadin.server ?? {};
            window.Vaadin.server.views = %s;""";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteExtractionIndexHtmlRequestListener.class);
    private final ClientRouteRegistry clientRouteRegistry;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a new listener instance with the given route registry.
     *
     * @param clientRouteRegistry
     *            the client route registry for getting the client side views
     */
    @Autowired
    public RouteExtractionIndexHtmlRequestListener(
            ClientRouteRegistry clientRouteRegistry) {
        this.clientRouteRegistry = clientRouteRegistry;
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
        clientRouteRegistry.getAllRoutes().forEach((route, config) -> {
            final AvailableViewInfo availableViewInfo = new AvailableViewInfo(
                    config.getTitle(), config.getRolesAllowed(),
                    config.isLoginRequired(), config.getRoute(),
                    config.isLazy(), config.isAutoRegistered(), config.menu(),
                    config.getRouteParameters());
            availableViews.put(route, availableViewInfo);
        });

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
