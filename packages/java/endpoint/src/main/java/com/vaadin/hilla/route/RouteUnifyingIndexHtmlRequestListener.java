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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.RouteParameterData;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.MenuAccessControl;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.server.auth.ViewAccessChecker;
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
            availableViews
                    .putAll(collectServerViews(response.getVaadinRequest()));
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
            clientRouteRegistry.loadLatestDevModeFileRoutesJsonIfNeeded(
                    deploymentConfiguration);
        }

        return clientRouteRegistry.getAllRoutes().values().stream()
                .filter(config -> config.getRouteParameters() == null
                        || config.getRouteParameters().isEmpty()
                        || config.getRouteParameters().values().stream()
                                .noneMatch(
                                        param -> param == RouteParamType.REQUIRED))
                .filter(config -> routeUtil.isRouteAllowed(isUserInRole,
                        isUserAuthenticated, config))
                .map(config -> new AvailableViewInfo(config.getTitle(),
                        config.getRolesAllowed(), config.isLoginRequired(),
                        config.getRoute(), config.isLazy(),
                        config.isAutoRegistered(), config.menu(),
                        config.getRouteParameters()))
                .collect(Collectors.toMap(AvailableViewInfo::route,
                        Function.identity()));
    }

    protected Map<String, AvailableViewInfo> collectServerViews(
            VaadinRequest vaadinRequest) {
        final VaadinService vaadinService = VaadinService.getCurrent();
        if (vaadinService == null) {
            LOGGER.debug(
                    "No VaadinService found, skipping server view collection");
            return Collections.emptyMap();
        }
        final var serverRouteRegistry = vaadinService.getRouter().getRegistry();

        var accessControls = Stream.of(accessControl, viewAccessChecker)
                .filter(Objects::nonNull).toList();

        var serverRoutes = Collections.<RouteData> emptyList();
        if (vaadinService.getInstantiator().getMenuAccessControl()
                .getPopulateClientSideMenu() == MenuAccessControl.PopulateClientMenu.ALWAYS
                || clientRouteRegistry.hasMainLayout()) {
            serverRoutes = serverRouteRegistry
                    .getRegisteredAccessibleMenuRoutes(vaadinRequest,
                            accessControls);
        }
        return serverRoutes.stream()
                .filter(serverView -> serverView.getTemplate() != null)
                .map(serverView -> {
                    final var viewClass = serverView.getNavigationTarget();
                    final var url = getRouteUrl(serverView);

                    final String title;
                    var pageTitle = AnnotationReader
                            .getAnnotationFor(viewClass, PageTitle.class)
                            .orElse(null);
                    if (pageTitle != null) {
                        title = pageTitle.value();
                    } else {
                        title = serverView.getNavigationTarget()
                                .getSimpleName();
                    }

                    final var menuConfig = Optional
                            .ofNullable(serverView.getMenuData())
                            .map(menu -> new ClientViewMenuConfig(
                                    (menu.getTitle() == null
                                            || menu.getTitle().isBlank())
                                                    ? title
                                                    : menu.getTitle(),
                                    menu.getOrder(), menu.getIcon(),
                                    menu.isExclude()))
                            .orElse(null);

                    return new AvailableViewInfo(title, null, false, url, false,
                            false, menuConfig,
                            serverView.getRouteParameters().values().stream());
                })
                .filter(view -> view.routeParameters().values().stream()
                        .noneMatch(param -> param == RouteParamType.REQUIRED))
                .collect(Collectors.toMap(AvailableViewInfo::route,
                        Function.identity()));
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

    /**
     * Get the route url for the route. If the route has optional parameters,
     * the url is stripped off from them.
     *
     * @param route
     *            route to get url for
     * @return url for the route
     */
    private static String getRouteUrl(RouteData route) {
        if (route.getRouteParameters() != null
                && !route.getRouteParameters().isEmpty()) {
            String editUrl = "/" + route.getTemplate();
            var params = route.getRouteParameters().values().stream()
                    .filter(param -> param.getTemplate().contains("?")
                            || param.getTemplate().contains("*"))
                    .toList();
            for (RouteParameterData param : params) {
                editUrl = editUrl.replace("/" + param.getTemplate(), "");
            }
            if (editUrl.isEmpty()) {
                editUrl = "/";
            }
            return editUrl;
        } else {
            return "/" + route.getTemplate();
        }
    }
}
