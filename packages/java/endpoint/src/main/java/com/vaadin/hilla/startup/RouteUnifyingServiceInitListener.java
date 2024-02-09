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
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.hilla.route.ClientRouteRegistry;
import com.vaadin.hilla.route.RouteUnifyingIndexHtmlRequestListener;
import com.vaadin.hilla.route.records.ClientViewConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Service init listener to add the
 * {@link RouteUnifyingIndexHtmlRequestListener} to the service.
 */
@Component
public class RouteUnifyingServiceInitListener
        implements VaadinServiceInitListener {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteUnifyingServiceInitListener.class);

    private final RouteUnifyingIndexHtmlRequestListener routeUnifyingIndexHtmlRequestListener;
    private final ClientRouteRegistry clientRouteRegistry;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a new instance of the listener.
     *
     * @param routeUnifyingIndexHtmlRequestListener
     *            the listener to add
     */
    @Autowired
    public RouteUnifyingServiceInitListener(
            RouteUnifyingIndexHtmlRequestListener routeUnifyingIndexHtmlRequestListener,
            ClientRouteRegistry clientRouteRegistry) {
        this.routeUnifyingIndexHtmlRequestListener = routeUnifyingIndexHtmlRequestListener;
        this.clientRouteRegistry = clientRouteRegistry;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        registerClientRoutes();
        event.addIndexHtmlRequestListener(
                routeUnifyingIndexHtmlRequestListener);
    }

    protected void registerClientRoutes() {
        try {
            final URL source = getClass()
                    .getResource("/META-INF/VAADIN/views.json");
            Map<String, ClientViewConfig> clientViews = new HashMap<>();
            if (source != null) {
                clientViews = mapper.readValue(source, new TypeReference<>() {
                });
            }

            clientRouteRegistry.clearRoutes();
            clientViews.forEach((route, clientView) -> {
                String title = clientView.title();
                if (title.isBlank()) {
                    title = clientView.route();
                }

                boolean hasMandatoryParam = route.contains(":")
                        && containsOnlyOptionalParams(route);
                new ClientViewConfig(title, clientView.rolesAllowed(), route,
                        clientView.lazy(), clientView.register(),
                        clientView.menu(), hasMandatoryParam,
                        clientView.other());
                clientRouteRegistry.addRoute(route, clientView);
            });
        } catch (IOException e) {
            LOGGER.warn("Failed extract client views from views.json", e);
        }
    }

    private boolean containsOnlyOptionalParams(String route) {
        return StringUtils.countMatches(route, ":") == StringUtils
                .countMatches(route, "?");
    }
}
