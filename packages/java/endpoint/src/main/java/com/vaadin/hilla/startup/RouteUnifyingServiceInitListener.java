/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.hilla.route.ClientRouteRegistry;
import com.vaadin.hilla.route.RouteExtractionIndexHtmlRequestListener;
import com.vaadin.hilla.route.records.ClientViewConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Service init listener to add the
 * {@link RouteExtractionIndexHtmlRequestListener} to the service and to
 * register client routes to {@link ClientRouteRegistry}.
 */
@Component
public class RouteUnifyingServiceInitListener
        implements VaadinServiceInitListener {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteUnifyingServiceInitListener.class);

    private final RouteExtractionIndexHtmlRequestListener routeExtractionIndexHtmlRequestListener;
    private final ClientRouteRegistry clientRouteRegistry;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a new instance of the listener.
     *
     * @param routeExtractionIndexHtmlRequestListener
     *            the listener to add
     * @param clientRouteRegistry
     *            the registry to add the client routes to
     */
    @Autowired
    public RouteUnifyingServiceInitListener(
            RouteExtractionIndexHtmlRequestListener routeExtractionIndexHtmlRequestListener,
            ClientRouteRegistry clientRouteRegistry) {
        this.routeExtractionIndexHtmlRequestListener = routeExtractionIndexHtmlRequestListener;
        this.clientRouteRegistry = clientRouteRegistry;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        registerClientRoutes(event.getSource().getDeploymentConfiguration());
        event.addIndexHtmlRequestListener(
                routeExtractionIndexHtmlRequestListener);
    }

    protected void registerClientRoutes(
            DeploymentConfiguration deploymentConfiguration) {
        try (var source = getViewsJsonAsResource(deploymentConfiguration)
                .openStream()) {
            if (source != null) {
                clientRouteRegistry.clearRoutes();
                registerAndRecurseChildren("",
                        mapper.readValue(source, new TypeReference<>() {
                        }));
            } else {
                LOGGER.warn("Failed to find views.json");
            }
        } catch (IOException e) {
            LOGGER.warn("Failed extract client views from views.json", e);
        }
    }

    private URL getViewsJsonAsResource(
            DeploymentConfiguration deploymentConfiguration)
            throws MalformedURLException {
        var isProductionMode = deploymentConfiguration.isProductionMode();
        if (isProductionMode) {
            return getClass().getResource("/META-INF/VAADIN/views.json");
        }
        return deploymentConfiguration.getFrontendFolder().toPath()
                .resolve("generated").resolve("views.json").toUri().toURL();
    }

    private void registerAndRecurseChildren(String basePath,
            ClientViewConfig view) {
        var path = view.getRoute() == null || view.getRoute().isEmpty()
                ? basePath
                : basePath + '/' + view.getRoute();
        if (view.getChildren() == null || view.getChildren().isEmpty()) {
            clientRouteRegistry.addRoute(path, view);
        } else {
            view.getChildren().forEach(child -> {
                child.setParent(view);
                registerAndRecurseChildren(path, child);
            });
        }
    }
}
