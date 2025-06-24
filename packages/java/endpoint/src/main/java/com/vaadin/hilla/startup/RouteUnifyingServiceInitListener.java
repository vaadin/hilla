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

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SynchronizedRequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import com.vaadin.flow.internal.menu.MenuRegistry;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.shared.JsonConstants;
import com.vaadin.hilla.HillaStats;
import com.vaadin.hilla.route.RouteUnifyingIndexHtmlRequestListener;
import com.vaadin.hilla.route.RouteUtil;
import com.vaadin.hilla.route.ServerAndClientViewsProvider;
import com.vaadin.hilla.route.RouteUnifyingConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    private final RouteUnifyingConfigurationProperties routeUnifyingConfigurationProperties;

    private final NavigationAccessControl accessControl;

    private final RouteUtil routeUtil;

    /**
     * Creates a new instance of the listener.
     *
     * @param routeUnifyingConfigurationProperties
     *            the configuration properties instance
     */
    @Autowired
    public RouteUnifyingServiceInitListener(RouteUtil routeUtil,
            RouteUnifyingConfigurationProperties routeUnifyingConfigurationProperties,
            @Nullable NavigationAccessControl accessControl) {
        this.routeUtil = routeUtil;
        this.routeUnifyingConfigurationProperties = routeUnifyingConfigurationProperties;
        this.accessControl = accessControl;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        var deploymentConfiguration = event.getSource()
                .getDeploymentConfiguration();
        LOGGER.debug("deploymentConfiguration.isReactEnabled() = {}",
                deploymentConfiguration.isReactEnabled());
        boolean hasHillaFsRoute = false;
        if (deploymentConfiguration.isReactEnabled()) {
            var serverAndClientViewsProvider = new ServerAndClientViewsProvider(
                    deploymentConfiguration, accessControl,
                    routeUnifyingConfigurationProperties
                            .isExposeServerRoutesToClient());
            var routeUnifyingIndexHtmlRequestListener = new RouteUnifyingIndexHtmlRequestListener(
                    serverAndClientViewsProvider);
            var deploymentMode = deploymentConfiguration.isProductionMode()
                    ? "PRODUCTION"
                    : "DEVELOPMENT";
            event.addIndexHtmlRequestListener(
                    routeUnifyingIndexHtmlRequestListener);
            if (!deploymentConfiguration.isProductionMode()) {
                // Dynamic updates are only useful during development
                event.addRequestHandler(new SynchronizedRequestHandler() {

                    @Override
                    public boolean synchronizedHandleRequest(
                            VaadinSession session, VaadinRequest request,
                            VaadinResponse response) throws IOException {
                        if ("routeinfo".equals(request.getParameter(
                                ApplicationConstants.REQUEST_TYPE_PARAMETER))) {
                            response.setContentType(
                                    JsonConstants.JSON_CONTENT_TYPE);
                            response.getWriter()
                                    .write(serverAndClientViewsProvider
                                            .createFileRoutesJson(request));
                            return true;
                        }
                        return false;
                    }

                });
            }
            LOGGER.debug(
                    "{} mode: Registered RouteUnifyingIndexHtmlRequestListener.",
                    deploymentMode);

            Map<String, AvailableViewInfo> clientMenus = MenuRegistry
                    .collectClientMenuItems(false, deploymentConfiguration,
                            null);

            hasHillaFsRoute = !clientMenus.isEmpty();
        }
        HillaStats.reportGenericHasFeatures(event.getSource(), hasHillaFsRoute);
    }
}
