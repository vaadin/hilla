package com.vaadin.hilla.route;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.MenuAccessControl;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.server.auth.ViewAccessChecker;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import com.vaadin.flow.internal.menu.MenuRegistry;
import com.vaadin.flow.server.menu.RouteParamType;

public class ServerAndClientViewsProvider {

    private final NavigationAccessControl accessControl;
    private final DeploymentConfiguration deploymentConfiguration;
    private final boolean exposeServerRoutesToClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ViewAccessChecker viewAccessChecker;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServerAndClientViewsProvider.class);

    /**
     * Creates a new listener instance with the given route registry.
     *
     * @param deploymentConfiguration
     *            the runtime deployment configuration
     * @param exposeServerRoutesToClient
     *            whether to expose server routes to the client
     */
    public ServerAndClientViewsProvider(
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

    public String createFileRoutesJson(VaadinRequest request)
            throws JsonProcessingException {
        final Map<String, AvailableViewInfo> availableViews = new HashMap<>(
                collectClientViews(request));
        final boolean hasAutoLayout = MenuRegistry.hasHillaMainLayout(
                request.getService().getDeploymentConfiguration());
        if (exposeServerRoutesToClient) {
            LOGGER.debug(
                    "Exposing server-side views to the client based on user configuration");
            availableViews.putAll(collectServerViews(hasAutoLayout));
        }

        return mapper.writeValueAsString(availableViews);
    }

    protected Map<String, AvailableViewInfo> collectClientViews(
            VaadinRequest request) {

        final Map<String, AvailableViewInfo> viewInfoMap = MenuRegistry
                .collectClientMenuItems(true, deploymentConfiguration, request);

        final Set<String> clientViewEntries = new HashSet<>(
                viewInfoMap.keySet());
        for (var path : clientViewEntries) {
            if (!viewInfoMap.containsKey(path)) {
                continue;
            }

            var viewInfo = viewInfoMap.get(path);
            // Remove routes with required parameters, including nested ones
            if (hasRequiredParameter(viewInfo)) {
                viewInfoMap.remove(path);
                if (viewInfo.children() != null) {
                    RouteUtil.removeChildren(viewInfoMap, viewInfo, path);
                }
                continue;
            }

            // Remove layouts
            if (viewInfo.children() != null) {
                viewInfoMap.remove(path);
            }
        }

        return viewInfoMap;
    }

    private static boolean hasRequiredParameter(AvailableViewInfo viewInfo) {
        final Map<String, RouteParamType> routeParameters = viewInfo
                .routeParameters();
        if (routeParameters != null && !routeParameters.isEmpty()
                && routeParameters.values().stream().anyMatch(
                        paramType -> paramType == RouteParamType.REQUIRED)) {
            return true;
        }

        // Nested routes could have parameters on the parent, check them also
        final AvailableViewInfo parentViewInfo = null;
        if (parentViewInfo != null) {
            return hasRequiredParameter(parentViewInfo);
        }

        return false;
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
