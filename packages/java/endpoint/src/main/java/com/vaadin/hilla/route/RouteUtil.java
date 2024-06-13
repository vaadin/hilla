package com.vaadin.hilla.route;

import com.vaadin.flow.internal.hilla.FileRouterRequestUtil;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import com.vaadin.flow.server.menu.MenuRegistry;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.http.server.RequestPath;
import org.springframework.util.AntPathMatcher;

/**
 * A container for utility methods related with Routes.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@Component
public class RouteUtil implements FileRouterRequestUtil {

    private Map<String, AvailableViewInfo> registeredRoutes = null;

    public RouteUtil() {
    }

    /**
     * Initializes a new instance of the RouteUtil class with the given route
     * map.
     *
     * @param registeredRoutes
     *            - the available unfiltered routes
     */
    public void setRoutes(
            final Map<String, AvailableViewInfo> registeredRoutes) {
        if (registeredRoutes == null) {
            this.registeredRoutes = null;
        } else {
            this.registeredRoutes = new HashMap<>(registeredRoutes);
        }
    }

    /**
     * Checks if the given request is allowed route to the user.
     *
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes allowed route,
     *         <code>false</code> otherwise
     */
    @Override
    public boolean isRouteAllowed(HttpServletRequest request) {
        if (registeredRoutes == null) {
            collectClientRoutes(request);
        }

        var viewConfig = getRouteData(request);

        return viewConfig.isPresent();
    }

    private static void filterClientViews(
            Map<String, AvailableViewInfo> configurations,
            HttpServletRequest request) {
        final boolean isUserAuthenticated = request.getUserPrincipal() != null;

        Set<String> clientEntries = new HashSet<>(configurations.keySet());
        for (String key : clientEntries) {
            if (!configurations.containsKey(key)) {
                continue;
            }
            AvailableViewInfo viewInfo = configurations.get(key);
            boolean routeValid = validateViewAccessible(viewInfo,
                    isUserAuthenticated, request::isUserInRole);

            if (!routeValid) {
                configurations.remove(key);
                if (viewInfo.children() != null
                        && !viewInfo.children().isEmpty()) {
                    // remove all children for unauthenticated parent.
                    removeChildren(configurations, viewInfo, key);
                }
            }
        }
    }

    /**
     * Check view against authentication state.
     * <p>
     * If not authenticated and login required -> invalid. If user doesn't have
     * correct roles -> invalid.
     *
     * @param viewInfo
     *            view info
     * @param isUserAuthenticated
     *            user authentication state
     * @param roleAuthentication
     *            method to authenticate if user has role
     * @return true if accessible, false if something is not authenticated
     */
    private static boolean validateViewAccessible(AvailableViewInfo viewInfo,
            boolean isUserAuthenticated,
            Predicate<? super String> roleAuthentication) {
        if (viewInfo.loginRequired() && !isUserAuthenticated) {
            return false;
        }
        String[] roles = viewInfo.rolesAllowed();
        return roles == null || roles.length == 0
                || Arrays.stream(roles).anyMatch(roleAuthentication);
    }

    public static void removeChildren(
            Map<String, AvailableViewInfo> configurations,
            AvailableViewInfo viewInfo, String parentPath) {
        for (AvailableViewInfo child : viewInfo.children()) {
            String childRoute = (parentPath + "/" + child.route()).replace("//",
                    "/");
            configurations.remove(childRoute);
            if (child.children() != null) {
                removeChildren(configurations, child, childRoute);
            }
        }
    }

    private Optional<AvailableViewInfo> getRouteData(
            HttpServletRequest request) {
        var requestPath = RequestPath.parse(request.getRequestURI(),
                request.getContextPath());
        Map<String, AvailableViewInfo> availableRoutes = new HashMap<>(
                registeredRoutes);
        filterClientViews(availableRoutes, request);
        return Optional.ofNullable(getRouteByPath(availableRoutes,
                requestPath.pathWithinApplication().value()));
    }

    private void collectClientRoutes(HttpServletRequest request) {
        ApplicationConfiguration config = ApplicationConfiguration
                .get(new VaadinServletContext(request.getServletContext()));
        setRoutes(MenuRegistry.collectClientMenuItems(false, config, null));
    }

    /**
     * Gets the client view configuration for the given route.
     *
     * @param path
     *            the URL path to get the client view configuration for
     * @return - the client view configuration for the given route
     */
    protected synchronized AvailableViewInfo getRouteByPath(
            Map<String, AvailableViewInfo> availableRoutes, String path) {
        final Set<String> routes = availableRoutes.keySet();
        final AntPathMatcher pathMatcher = new AntPathMatcher();
        return Stream.of(addTrailingSlash(path), removeTrailingSlash(path))
                .map(p -> {
                    for (String route : routes) {
                        if (pathMatcher.match(route, p)) {
                            return availableRoutes.get(route);
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private String addTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + '/';
    }

    private String removeTrailingSlash(String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
