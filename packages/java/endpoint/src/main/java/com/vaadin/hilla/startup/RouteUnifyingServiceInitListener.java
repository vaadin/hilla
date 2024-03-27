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

package com.vaadin.hilla.startup;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.hilla.route.ClientRouteRegistry;
import com.vaadin.hilla.route.RouteUnifyingIndexHtmlRequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service init listener to add the
 * {@link RouteUnifyingIndexHtmlRequestListener} to the service and to register
 * client routes to {@link ClientRouteRegistry}.
 */
@Component
public class RouteUnifyingServiceInitListener
        implements VaadinServiceInitListener {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteUnifyingServiceInitListener.class);

    private final ClientRouteRegistry clientRouteRegistry;

    /**
     * Creates a new instance of the listener.
     *
     * @param clientRouteRegistry
     *            the registry to add the client routes to
     */
    @Autowired
    public RouteUnifyingServiceInitListener(
            ClientRouteRegistry clientRouteRegistry) {
        this.clientRouteRegistry = clientRouteRegistry;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        var deploymentConfiguration = event.getSource()
                .getDeploymentConfiguration();
        if (deploymentConfiguration.isReactEnabled()) {
            var routeUnifyingIndexHtmlRequestListener = new RouteUnifyingIndexHtmlRequestListener(
                    clientRouteRegistry, deploymentConfiguration);
            var deploymentMode = deploymentConfiguration.isProductionMode()
                    ? "PRODUCTION"
                    : "DEVELOPMENT";
            event.addIndexHtmlRequestListener(
                    routeUnifyingIndexHtmlRequestListener);
            LOGGER.debug(
                    "{} mode: Registered RouteUnifyingIndexHtmlRequestListener.",
                    deploymentMode);
        }
    }
}
