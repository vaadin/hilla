package com.vaadin.hilla.route;

import com.vaadin.flow.internal.hilla.FileRouterRequestUtil;
import com.vaadin.hilla.route.records.ClientViewConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.http.server.RequestPath;

/**
 * A container for utility methods related with Routes.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@Component
public class RouteUtil implements FileRouterRequestUtil {

    private final ClientRouteRegistry registry;

    /**
     * Initializes a new instance of the RouteUtil class with the given
     * registry.
     *
     * @param registry
     *            - the registry to use
     */
    @Autowired
    public RouteUtil(final ClientRouteRegistry registry) {
        this.registry = registry;
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
        var viewConfig = getRouteData(request);
        var isUserAuthenticated = request.getUserPrincipal() != null;
        return viewConfig.filter(
                clientViewConfig -> isRouteAllowed(request::isUserInRole,
                        isUserAuthenticated, clientViewConfig))
                .isPresent();
    }

    boolean isRouteAllowed(Predicate<? super String> isUserInRole,
            boolean isUserAuthenticated, ClientViewConfig viewConfig) {
        boolean isAllowed;

        if (viewConfig.isLoginRequired() && !isUserAuthenticated) {
            isAllowed = false;
        } else {
            var rolesAllowed = viewConfig.getRolesAllowed();

            if (rolesAllowed != null) {
                isAllowed = Arrays.stream(rolesAllowed).anyMatch(isUserInRole);
            } else {
                isAllowed = true;
            }
        }

        if (isAllowed && viewConfig.getParent() != null) {
            isAllowed = isRouteAllowed(isUserInRole, isUserAuthenticated,
                    viewConfig.getParent());
        }

        return isAllowed;
    }

    private Optional<ClientViewConfig> getRouteData(
            HttpServletRequest request) {
        var requestPath = RequestPath.parse(request.getRequestURI(),
                request.getContextPath());
        return Optional.ofNullable(registry
                .getRouteByPath(requestPath.pathWithinApplication().value()));
    }
}
