/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.startup;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.server.auth.ViewAccessChecker;
import com.vaadin.hilla.HillaStats;
import com.vaadin.hilla.route.ClientRouteRegistry;
import com.vaadin.hilla.route.RouteUnifyingIndexHtmlRequestListener;
import com.vaadin.hilla.route.RouteUnifyingConfigurationProperties;
import com.vaadin.hilla.route.RouteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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

    private final RouteUtil routeUtil;

    private final RouteUnifyingConfigurationProperties routeUnifyingConfigurationProperties;

    private final NavigationAccessControl accessControl;

    private final ViewAccessChecker viewAccessChecker;

    /**
     * Creates a new instance of the listener.
     *
     * @param clientRouteRegistry
     *            the registry to add the client routes to
     * @param routeUtil
     *            the ClientRouteRegistry aware utility for checking if user is
     *            allowed to access a route
     * @param routeUnifyingConfigurationProperties
     *            the configuration properties instance
     */
    @Autowired
    public RouteUnifyingServiceInitListener(
            ClientRouteRegistry clientRouteRegistry, RouteUtil routeUtil,
            RouteUnifyingConfigurationProperties routeUnifyingConfigurationProperties,
            @Nullable NavigationAccessControl accessControl,
            @Nullable ViewAccessChecker viewAccessChecker) {
        this.clientRouteRegistry = clientRouteRegistry;
        this.routeUtil = routeUtil;
        this.routeUnifyingConfigurationProperties = routeUnifyingConfigurationProperties;
        this.accessControl = accessControl;
        this.viewAccessChecker = viewAccessChecker;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        var deploymentConfiguration = event.getSource()
                .getDeploymentConfiguration();
        LOGGER.debug("deploymentConfiguration.isReactEnabled() = {}",
                deploymentConfiguration.isReactEnabled());
        boolean hasHillaFsRoute = false;
        if (deploymentConfiguration.isReactEnabled()) {
            var routeUnifyingIndexHtmlRequestListener = new RouteUnifyingIndexHtmlRequestListener(
                    clientRouteRegistry, deploymentConfiguration, routeUtil,
                    accessControl, viewAccessChecker,
                    routeUnifyingConfigurationProperties
                            .isExposeServerRoutesToClient());
            var deploymentMode = deploymentConfiguration.isProductionMode()
                    ? "PRODUCTION"
                    : "DEVELOPMENT";
            event.addIndexHtmlRequestListener(
                    routeUnifyingIndexHtmlRequestListener);
            LOGGER.debug(
                    "{} mode: Registered RouteUnifyingIndexHtmlRequestListener.",
                    deploymentMode);

            clientRouteRegistry.registerClientRoutes(deploymentConfiguration,
                    LocalDateTime.now());

            hasHillaFsRoute = !clientRouteRegistry.getAllRoutes().isEmpty();
        }
        HillaStats.reportGenericHasFeatures(event.getSource(), hasHillaFsRoute);
    }
}
